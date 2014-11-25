package com.emprovise.api.rally.param;


public enum Severity {
    CRITICAL("Critical"),
    MAJOR("Major"),
    MINOR("Minor"),
    INCIDENTAL("Incidental");

    private String name;

    private Severity(String name) {
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
