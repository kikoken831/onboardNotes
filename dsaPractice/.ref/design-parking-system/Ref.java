class Ref implements Problem {

    public Spec spec() {
        return Spec.design("ParkingSystem", T.INT, T.INT, T.INT)
            .op("addCar", T.BOOL, T.INT)
            .exampleCalls("[\"ParkingSystem\",\"addCar\",\"addCar\",\"addCar\",\"addCar\"]",
                          "[[1,1,0],[1],[2],[3],[1]]",
                          "[null,true,true,false,false]")
            .exampleCalls("[\"ParkingSystem\",\"addCar\",\"addCar\",\"addCar\"]",
                          "[[0,0,2],[3],[3],[3]]",
                          "[null,true,true,false]")
            .gen(g -> {
                Calls.Builder b = new Calls.Builder("ParkingSystem",
                        g.nextInt(0, 4), g.nextInt(0, 4), g.nextInt(0, 4));
                int n = g.size(1, 40);
                for (int i = 0; i < n; i++) b.call("addCar", g.nextInt(1, 3));
                return new Object[]{ b.build() };
            })
            .patterns("Design", "Counting", "Simulation")
            .limitMs(2000)
            .maxN(100000);
    }
}

class ParkingSystem {
    private final int[] free = new int[4];

    public ParkingSystem(int big, int medium, int small) {
        Pre.check(big >= 0 && medium >= 0 && small >= 0, "slot counts >= 0");
        free[1] = big; free[2] = medium; free[3] = small;
    }

    public boolean addCar(int carType) {
        Pre.check(carType >= 1 && carType <= 3, "carType is 1, 2 or 3");
        if (free[carType] == 0) return false;
        free[carType]--;
        return true;
    }
}
