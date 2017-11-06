package com.emprovise.api.google.chrome.dao;

public class Urls {

    private Integer id;
    private String url;
    private String title;
    private Integer visit_count;
    private Integer typed_count;
    private String last_visit_time;
    private Boolean hidden;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getVisitCount() {
        return visit_count;
    }

    public void setVisitCount(Integer visit_count) {
        this.visit_count = visit_count;
    }

    public Integer getTypedCount() {
        return typed_count;
    }

    public void setTypedCount(Integer typed_count) {
        this.typed_count = typed_count;
    }

    public String getLastVisitTime() {
        return last_visit_time;
    }

    public void setLastVisitTime(String last_visit_time) {
        this.last_visit_time = last_visit_time;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
