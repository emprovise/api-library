package com.emprovise.api.google.youtube.datatype;

public enum Order {

    DATE("date"),
    RATING("rating"),
    RELEVANCE("relevance"),
    TITLE("title"),
    VIDEOCOUNT("videoCount"),
    VIEWCOUNT("viewCount");

    private String value;

    Order(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
