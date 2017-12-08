package com.emprovise.api.rally.type;


public enum TestType {
    ACCEPTANCE("Acceptance"),
    EXPLORATORY("Exploratory"),
    FUNCTIONAL("Functional"),
    PERFORMANCE("Performance"),
    REGRESSION("Regression"),
    USABILITY("Usability"),
    USER_INTERFACE("User Interface");

    private String name;

    private TestType(String name) {
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
