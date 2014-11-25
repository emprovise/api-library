package com.emprovise.api.rally.param;


public enum DefectState {
    SUBMITTED("Submitted"),
    OPEN("Open"),
    FIXED("Fixed"),
    DEPLOYED("Deployed"),
    CLOSED("Closed");

    private String name;

    private DefectState(String name) {
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
