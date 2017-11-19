package com.emprovise.api.amazon.ws.common.types;

public enum InternetProtocol {

    TCP("tcp"),
    UDP("udp");

    private final String value;

    InternetProtocol(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
