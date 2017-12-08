package com.emprovise.api.rally.type;


public enum ScheduleState {
    BACKLOG("Backlog"),
    DEFINED("Defined"),
    IN_PROGRESS("In-Progress"),
    COMPLETED("Completed"),
    ACCEPTED("Accepted");

    private String name;

    private ScheduleState(String name) {
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

