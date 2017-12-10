package com.emprovise.api.flowdock.model;

import com.emprovise.api.flowdock.type.MessageType;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private String event;
    private String content;
    private List<String> tags;
    private String message;
    private String thread_id;
    private String external_thread_id;

    public String getEvent() {
        return event;
    }

    public void setEvent(MessageType messageType) {
        this.event = messageType.value();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void addTags(String tag) {
        if(tags == null) {
            this.tags = new ArrayList<>();
        }

        this.tags.add(tag);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThread_id() {
        return thread_id;
    }

    public void setThread_id(String thread_id) {
        this.thread_id = thread_id;
    }

    public String getExternal_thread_id() {
        return external_thread_id;
    }

    public void setExternal_thread_id(String external_thread_id) {
        this.external_thread_id = external_thread_id;
    }
}
