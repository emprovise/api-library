package com.emprovise.api.rally.type;

public enum TestMethod {
    MANUAL("Manual"),
    AUTOMATED("Automated");

    private String name;

    private TestMethod(String name) {
        this.name = name;
    }

    public String value() {
        return name;
    }

    @Override
    public String toString(){
        return name;
    }
}
