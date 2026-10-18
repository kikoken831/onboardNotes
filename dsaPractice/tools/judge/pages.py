"""Generate a static GitHub Pages dashboard from progress data."""

import json
import os
import time
from collections import Counter
from datetime import datetime, timedelta

from . import catalogue, progress

DOCS_DIR = os.path.join(os.path.dirname(catalogue.ROOT), "docs")

_TEMPLATE_TOP = r"""<title>DSA Practice</title>
<meta name="claude:viewport-fit-sides" content="cover">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600&family=Inter:wght@400;500;600;700&display=swap">
<style>
/* layout: single-column dashboard, heatmap up top, stats + tables below */
:root {
  --bg: #0d1117;
  --bg-card: #161b22;
  --fg: #c9d1d9;
  --fg-muted: #8b949e;
  --fg-bright: #e6edf3;
  --accent: #58a6ff;
  --green-0: #161b22;
  --green-1: #0e4429;
  --green-2: #006d32;
  --green-3: #26a641;
  --green-4: #39d353;
  --border: #30363d;
  --red: #f85149;
  --yellow: #d29922;
  --orange: #db6d28;
  color-scheme: dark;
}
@media (prefers-color-scheme: light) {
  :root:not([data-theme="dark"]) {
    --bg: #f6f8fa;
    --bg-card: #ffffff;
    --fg: #1f2328;
    --fg-muted: #656d76;
    --fg-bright: #1f2328;
    --accent: #0969da;
    --green-0: #ebedf0;
    --green-1: #9be9a8;
    --green-2: #40c463;
    --green-3: #30a14e;
    --green-4: #216e39;
    --border: #d0d7de;
    --red: #cf222e;
    --yellow: #bf8700;
    --orange: #bc4c00;
    color-scheme: light;
  }
}
:root[data-theme="light"] {
  --bg: #f6f8fa;
  --bg-card: #ffffff;
  --fg: #1f2328;
  --fg-muted: #656d76;
  --fg-bright: #1f2328;
  --accent: #0969da;
  --green-0: #ebedf0;
  --green-1: #9be9a8;
  --green-2: #40c463;
  --green-3: #30a14e;
  --green-4: #216e39;
  --border: #d0d7de;
  --red: #cf222e;
  --yellow: #bf8700;
  --orange: #bc4c00;
  color-scheme: light;
}

* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  background: var(--bg);
  color: var(--fg);
  font-family: 'Inter', -apple-system, sans-serif;
  padding-left: max(16px, env(safe-area-inset-left, 0px));
  padding-right: max(16px, env(safe-area-inset-right, 0px));
  padding-block: 24px;
}
.wrap { max-width: 960px; margin: 0 auto; display: flex; flex-direction: column; gap: 28px; }
h1 { font-size: 1.6rem; font-weight: 700; color: var(--fg-bright); }
h1 small { font-weight: 400; color: var(--fg-muted); font-size: 0.85rem; margin-left: 8px; }
h2 { font-size: 1rem; font-weight: 600; color: var(--fg-bright); margin-bottom: 12px; }

.stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 12px; }
.stat {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 16px;
}
.stat-value { font-family: 'JetBrains Mono', monospace; font-size: 1.8rem; font-weight: 600; color: var(--fg-bright); font-variant-numeric: tabular-nums; }
.stat-label { font-size: 0.78rem; color: var(--fg-muted); margin-top: 2px; }

.card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 20px;
}

/* heatmap */
.heatmap-scroll { overflow-x: auto; }
.heatmap { display: flex; gap: 3px; }
.heatmap-col { display: flex; flex-direction: column; gap: 3px; }
.heatmap-cell {
  width: 12px; height: 12px;
  border-radius: 2px;
  background: var(--green-0);
}
.heatmap-cell[data-level="1"] { background: var(--green-1); }
.heatmap-cell[data-level="2"] { background: var(--green-2); }
.heatmap-cell[data-level="3"] { background: var(--green-3); }
.heatmap-cell[data-level="4"] { background: var(--green-4); }
.heatmap-cell:hover { outline: 1px solid var(--fg-muted); outline-offset: -1px; }
.heatmap-months { display: flex; font-size: 0.7rem; color: var(--fg-muted); margin-bottom: 4px; }
.heatmap-months span { flex: none; }
.heatmap-days { display: flex; flex-direction: column; gap: 3px; font-size: 0.65rem; color: var(--fg-muted); margin-right: 6px; padding-top: 1px; }
.heatmap-days span { height: 12px; line-height: 12px; }
.heatmap-wrap { display: flex; }
.heatmap-legend { display: flex; align-items: center; gap: 4px; font-size: 0.7rem; color: var(--fg-muted); margin-top: 8px; justify-content: flex-end; }
.heatmap-legend .heatmap-cell { width: 10px; height: 10px; cursor: default; }

.tooltip {
  position: fixed;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 4px;
  padding: 6px 10px;
  font-size: 0.75rem;
  color: var(--fg);
  pointer-events: none;
  z-index: 100;
  white-space: nowrap;
  display: none;
}

/* tier table */
.tier-table { width: 100%; border-collapse: collapse; font-variant-numeric: tabular-nums; }
.tier-table th { text-align: left; font-size: 0.72rem; color: var(--fg-muted); font-weight: 500; padding: 6px 10px; border-bottom: 1px solid var(--border); text-transform: uppercase; letter-spacing: 0.04em; }
.tier-table td { padding: 8px 10px; border-bottom: 1px solid var(--border); font-size: 0.85rem; }
.tier-table tr:last-child td { border-bottom: none; }
.tier-table .num { text-align: right; font-family: 'JetBrains Mono', monospace; font-size: 0.82rem; }

.bar-track { width: 100%; height: 6px; background: var(--green-0); border-radius: 3px; overflow: hidden; min-width: 80px; }
.bar-fill { height: 100%; background: var(--green-3); border-radius: 3px; transition: width 0.4s ease; }

/* due list */
.due-list { list-style: none; display: flex; flex-direction: column; gap: 6px; }
.due-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 12px;
  background: var(--bg);
  border-radius: 6px;
  font-size: 0.85rem;
}
.due-item .slug { font-family: 'JetBrains Mono', monospace; font-weight: 600; color: var(--fg-bright); min-width: 0; }
.due-item .meta { color: var(--fg-muted); font-size: 0.75rem; margin-left: auto; white-space: nowrap; }
.badge {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
  flex-shrink: 0;
}
.badge-easy { background: #238636; color: #fff; }
.badge-medium { background: var(--yellow); color: #fff; }
.badge-hard { background: var(--red); color: #fff; }
.badge-drill { background: var(--accent); color: #fff; }

.group-label { font-size: 0.78rem; color: var(--fg-muted); padding: 10px 0 4px; }
.empty { color: var(--fg-muted); font-size: 0.85rem; font-style: italic; }

@media (max-width: 600px) {
  .stats { grid-template-columns: repeat(2, 1fr); }
  .due-item { flex-wrap: wrap; }
  .due-item .meta { margin-left: 0; }
}
</style>

<div class="wrap">
  <h1>DSA Practice <small>progress dashboard</small></h1>
  <div class="stats" id="stats"></div>
  <div class="card">
    <h2>Activity</h2>
    <div class="heatmap-scroll" id="heatmap-container"></div>
  </div>
  <div class="card">
    <h2>Progress by Tier</h2>
    <div style="overflow-x:auto"><table class="tier-table" id="tier-table"></table></div>
  </div>
  <div class="card">
    <h2>Due Today</h2>
    <div id="due-today"></div>
  </div>
  <div class="card">
    <h2>Upcoming</h2>
    <div id="upcoming"></div>
  </div>
  <div style="text-align:center;padding:16px 0">
    <small style="color:var(--fg-muted)">Generated <span id="gen-time"></span></small>
  </div>
</div>
<div class="tooltip" id="tooltip"></div>

<script>
const DATA = __DATA_PLACEHOLDER__;

function render() {
  const d = DATA;
  const now = d.generated;
  document.getElementById('gen-time').textContent = new Date(now * 1000).toLocaleDateString('en-US', { year:'numeric', month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' });

  // stats
  const statsEl = document.getElementById('stats');
  const hours = (d.total_practice_seconds / 3600).toFixed(1);
  const stats = [
    [d.total_solved, 'solved'],
    [d.total_implemented, 'implemented'],
    [d.total_due, 'due today'],
    [d.total_upcoming, 'upcoming'],
    [d.current_streak, 'day streak'],
    [hours + 'h', 'practiced'],
  ];
  statsEl.innerHTML = stats.map(([v, l]) =>
    `<div class="stat"><div class="stat-value">${v}</div><div class="stat-label">${l}</div></div>`
  ).join('');

  // heatmap
  renderHeatmap(d.daily_activity);

  // tier table
  const table = document.getElementById('tier-table');
  table.innerHTML = `<thead><tr><th>Tier</th><th></th><th class="num">Ready</th><th class="num">Solved</th><th class="num">Due</th><th style="min-width:80px"></th></tr></thead><tbody>` +
    d.tiers.map(t => {
      const pct = t.implemented ? Math.round(t.solved / t.implemented * 100) : 0;
      return `<tr>
        <td style="font-weight:600">T${t.tier}</td>
        <td style="color:var(--fg-muted)">${t.name}</td>
        <td class="num">${t.implemented}/${t.total}</td>
        <td class="num">${t.solved}</td>
        <td class="num">${t.due || ''}</td>
        <td><div class="bar-track"><div class="bar-fill" style="width:${pct}%"></div></div></td>
      </tr>`;
    }).join('') + `</tbody>`;

  // due today
  const dueEl = document.getElementById('due-today');
  if (!d.due_today.length) {
    dueEl.innerHTML = '<p class="empty">No re-solves due today</p>';
  } else {
    dueEl.innerHTML = '<ul class="due-list">' + d.due_today.map(p => {
      const badge = diffBadge(p.difficulty);
      const overdue = p.overdue_days >= 1 ? `<span style="color:var(--orange)">${p.overdue_days}d overdue</span>` : '';
      return `<li class="due-item">${badge}<span class="slug">${p.slug}</span><span class="meta">${p.patterns} ${overdue}</span></li>`;
    }).join('') + '</ul>';
  }

  // upcoming
  const upEl = document.getElementById('upcoming');
  if (!d.upcoming.length) {
    upEl.innerHTML = '<p class="empty">No upcoming re-solves scheduled</p>';
  } else {
    const groups = {};
    d.upcoming.forEach(p => {
      (groups[p.days_until] = groups[p.days_until] || []).push(p);
    });
    let html = '';
    Object.keys(groups).sort((a,b) => a - b).forEach(days => {
      html += `<div class="group-label">in ${days} day${days == 1 ? '' : 's'}</div><ul class="due-list">`;
      groups[days].forEach(p => {
        const badge = diffBadge(p.difficulty);
        html += `<li class="due-item">${badge}<span class="slug">${p.slug}</span><span class="meta">${p.patterns}</span></li>`;
      });
      html += '</ul>';
    });
    upEl.innerHTML = html;
  }
}

function diffBadge(diff) {
  const cls = {'Easy':'badge-easy','Medium':'badge-medium','Hard':'badge-hard','Drill':'badge-drill'}[diff] || 'badge-drill';
  return `<span class="badge ${cls}">${diff || 'Drill'}</span>`;
}

function localDateStr(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function renderHeatmap(dailyActivity) {
  const container = document.getElementById('heatmap-container');
  const tooltip = document.getElementById('tooltip');
  const today = new Date();
  today.setHours(0,0,0,0);

  const weeks = 26;
  const startDay = new Date(today);
  startDay.setDate(startDay.getDate() - (weeks * 7 - 1) - startDay.getDay());

  const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  const dayLabels = ['','Mon','','Wed','','Fri',''];
  const colWidth = 15; // cell 12px + gap 3px

  // first pass: build columns and record which column each month starts at
  const columns = [];
  const monthMarkers = [];
  let currentDate = new Date(startDay);
  let lastMonth = -1;

  while (currentDate <= today) {
    const colStart = new Date(currentDate);
    if (colStart.getMonth() !== lastMonth) {
      monthMarkers.push({ month: colStart.getMonth(), col: columns.length });
      lastMonth = colStart.getMonth();
    }

    let col = '<div class="heatmap-col">';
    for (let dow = currentDate.getDay(); dow < 7; dow++) {
      if (currentDate > today) {
        col += '<div style="width:12px;height:12px"></div>';
      } else {
        const dateStr = localDateStr(currentDate);
        const entry = dailyActivity[dateStr];
        const solves = entry ? entry.solves : 0;
        const attempts = entry ? entry.attempts : 0;
        const hours = entry ? entry.hours : 0;
        const level = solves === 0 ? 0 : solves <= 2 ? 1 : solves <= 5 ? 2 : solves <= 10 ? 3 : 4;
        col += `<div class="heatmap-cell" data-level="${level}" data-date="${dateStr}" data-solves="${solves}" data-attempts="${attempts}" data-hours="${hours}"></div>`;
      }
      currentDate.setDate(currentDate.getDate() + 1);
    }
    col += '</div>';
    columns.push(col);
  }

  // build month header using absolute positioning based on column index
  let monthHeader = '<div class="heatmap-months" style="padding-left:28px;position:relative;height:16px">';
  for (const m of monthMarkers) {
    monthHeader += `<span style="position:absolute;left:${m.col * colWidth}px">${monthNames[m.month]}</span>`;
  }
  monthHeader += '</div>';

  const dayLabelHtml = '<div class="heatmap-days">' + dayLabels.map(l => `<span>${l}</span>`).join('') + '</div>';

  container.innerHTML = monthHeader +
    '<div class="heatmap-wrap">' + dayLabelHtml + '<div class="heatmap" style="flex:1">' + columns.join('') + '</div></div>' +
    '<div class="heatmap-legend">Less ' +
    [0,1,2,3,4].map(l => `<div class="heatmap-cell" data-level="${l}"></div>`).join('') +
    ' More</div>';

  container.addEventListener('mouseover', e => {
    const cell = e.target.closest('.heatmap-cell[data-date]');
    if (!cell) { tooltip.style.display = 'none'; return; }
    const solves = cell.dataset.solves;
    const attempts = cell.dataset.attempts;
    const hours = cell.dataset.hours;
    const date = cell.dataset.date;
    const hoursPart = hours > 0 ? `, ${hours}h practiced` : '';
    tooltip.textContent = `${solves} solve${solves == 1 ? '' : 's'}, ${attempts} attempt${attempts == 1 ? '' : 's'}${hoursPart} on ${date}`;
    tooltip.style.display = 'block';
    const r = cell.getBoundingClientRect();
    tooltip.style.left = r.left + 'px';
    tooltip.style.top = (r.top - 30) + 'px';
  });
  container.addEventListener('mouseout', () => { tooltip.style.display = 'none'; });
}

render();
</script>"""


def _daily_activity(state):
    """Count solves, attempts, and practice hours per calendar day."""
    solves = Counter()
    attempts = Counter()
    day_timestamps = {}
    session_gap = 2 * 3600
    min_session = 5 * 60

    for e in state["problems"].values():
        for h in e.get("history", []):
            day = datetime.fromtimestamp(h["t"]).strftime("%Y-%m-%d")
            attempts[day] += 1
            if h["v"] == "ACCEPTED" and not h.get("pinned_only"):
                solves[day] += 1
            day_timestamps.setdefault(day, []).append(h["t"])

    hours = {}
    for day, ts in day_timestamps.items():
        ts.sort()
        total = 0
        session_start = ts[0]
        prev = ts[0]
        for t in ts[1:]:
            if t - prev > session_gap:
                total += max(min_session, prev - session_start)
                session_start = t
            prev = t
        total += max(min_session, prev - session_start)
        hours[day] = round(total / 3600, 1)

    all_days = set(solves) | set(attempts)
    return {d: {"solves": solves[d], "attempts": attempts[d],
                "hours": hours.get(d, 0)} for d in all_days}


def _streak(daily):
    """Current consecutive-day streak (days with solves) ending today or yesterday."""
    if not daily:
        return 0
    solve_days = {d for d, v in daily.items() if v["solves"] > 0}
    today = datetime.now().date()
    d = today
    count = 0
    if d.isoformat() not in solve_days:
        d -= timedelta(days=1)
    while d.isoformat() in solve_days:
        count += 1
        d -= timedelta(days=1)
    return count


def build():
    """Generate docs/index.html with embedded progress data."""
    state = progress.load()
    all_problems = catalogue.all_problems()

    # tier stats
    tiers = {}
    for p in all_problems:
        t = p["tier"]
        row = tiers.setdefault(t, {"tier": t, "name": catalogue.TIER_NAMES.get(t, ""),
                                    "total": 0, "implemented": 0, "solved": 0, "due": 0})
        row["total"] += 1
        if catalogue.is_implemented(p):
            row["implemented"] += 1
        e = state["problems"].get(p["slug"])
        if e:
            if e["solved"]:
                row["solved"] += 1
            if e.get("due") and progress._is_due(e["due"]):
                row["due"] += 1

    # due today
    due_slugs = progress.due_now(state)
    due_today = []
    now = time.time()
    for slug in due_slugs:
        p = catalogue.by_slug(slug)
        if not p:
            continue
        e = state["problems"][slug]
        due_today.append({
            "slug": slug,
            "difficulty": p.get("difficulty", ""),
            "patterns": ", ".join(p.get("pattern", [])[:3]),
            "overdue_days": max(0, int((now - e["due"]) / 86400)),
        })

    # upcoming
    upcoming_raw = progress.upcoming(state)
    upcoming_list = []
    for due_epoch, slug in upcoming_raw:
        p = catalogue.by_slug(slug)
        if not p:
            continue
        upcoming_list.append({
            "slug": slug,
            "difficulty": p.get("difficulty", ""),
            "patterns": ", ".join(p.get("pattern", [])[:3]),
            "days_until": max(1, int((due_epoch - now) / 86400 + 0.5)),
        })

    daily = _daily_activity(state)

    data = {
        "generated": now,
        "total_solved": sum(1 for e in state["problems"].values() if e.get("solved")),
        "total_implemented": sum(1 for p in all_problems if catalogue.is_implemented(p)),
        "total_due": len(due_slugs),
        "total_upcoming": len(upcoming_list),
        "total_attempts": sum(e.get("attempts", 0) for e in state["problems"].values()),
        "total_practice_seconds": progress.total_practice_seconds(state),
        "current_streak": _streak(daily),
        "daily_activity": daily,
        "tiers": [tiers[t] for t in sorted(tiers)],
        "due_today": due_today,
        "upcoming": upcoming_list,
    }

    html = _TEMPLATE_TOP.replace("__DATA_PLACEHOLDER__", json.dumps(data))
    # Split at first <div to separate head content from body content
    head_end = html.index("<div")
    head_content = html[:head_end]
    body_content = html[head_end:]
    return ("<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n"
            "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n"
            + head_content +
            "</head>\n<body>\n" + body_content + "\n</body>\n</html>\n")
