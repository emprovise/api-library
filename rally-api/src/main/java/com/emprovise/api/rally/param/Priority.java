package com.emprovise.api.rally.param;

public enum Priority {
    RESOLVE_IMMEDIATELY("Resolve Immediately"),
    HIGH_ATTENTION("High Attention"),
    NORMAL("Normal"),
    LOW("Low");

    private String name;

    private Priority(String name) {
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
