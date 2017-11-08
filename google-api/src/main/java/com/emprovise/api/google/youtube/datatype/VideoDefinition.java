package com.emprovise.api.google.youtube.datatype;

public enum VideoDefinition {

    ANY("any"),
    HIGH("high"),
    STANDARD("standard");

    private String value;

    VideoDefinition(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
