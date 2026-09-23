# Design Parking System

LeetCode 1603 · Easy · https://leetcode.com/problems/design-parking-system/

## Brief

A car park has `big`, `medium` and `small` slots. `addCar(carType)` — `1` big, `2` medium,
`3` small — parks the car in a slot **of its own size** if one is free and returns `true`,
else `false`. Cars never leave.

Design problem: implement `class ParkingSystem`.

## Constraints

- `0 <= big, medium, small <= 1000`
- `carType` is 1, 2 or 3
- at most 1000 calls

## Examples

```
["ParkingSystem","addCar","addCar","addCar","addCar"]
[[1,1,0],[1],[2],[3],[1]]
->  [null,true,true,false,false]
```

## Signature

```java
class ParkingSystem {

    public ParkingSystem(int big, int medium, int small) {
        
    }

    public boolean addCar(int carType) {
        
    }
}
```

## Target complexity

O(1) per call.

## The skill

State held in fields, mutated by calls — the smallest possible design problem. Use the
`carType` directly as an index into an array of counts rather than a three-way `if`. Note a
car can't use a bigger slot here; that rule is what makes it simple.

## Hints

<details><summary>Hint 1</summary>

`int[] free = new int[4]`, index 1..3.

</details>
