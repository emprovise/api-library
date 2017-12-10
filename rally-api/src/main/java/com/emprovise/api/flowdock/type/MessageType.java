package com.emprovise.api.flowdock.type;

public enum MessageType {

    MESSAGE("message"),
    COMMENT("comment"),
    STATUS("status"),
    FILE("file"),
    ACTION("action"),
    TAG_CHANGE("tag-change"),
    MESSAGE_EDIT("message-edit"),
    ACTIVITY("activity"),
    DISCUSSION("discussion");

    private String value;

    MessageType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
