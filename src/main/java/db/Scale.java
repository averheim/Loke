package db;

public enum Scale {
    HUNDRED("hundred dollars", 10),
    DEFAULT("dollars", 1);

    private final String name;
    private final int divideBy;

    Scale(String name, int divideBy) {
        this.name = name;
        this.divideBy = divideBy;
    }

    public String getName() {
        return name;
    }

    public int getDivideBy() {
        return divideBy;
    }
}
