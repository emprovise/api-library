package com.emprovise.api.google.youtube.datatype;

public enum VideoDuration {

    ANY("any"),
    LONG("long"),
    MEDIUM("medium"),
    SHORT("short");

    private String value;

    VideoDuration(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
