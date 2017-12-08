package com.emprovise.api.rally.type;

public enum Param {
    NAME("Name"),
    DESCRIPTION("Description"),
    STATE("State"),
    OWNER("Owner"),
    PROJECT("Project"),
    ENVIRONMENT("Environment"),
    BLOCKED("Blocked"),
    PRIORITY("Priority"),
    TAGS("Tags"),
    NOTES("Notes"),
    SEVERITY("Severity"),
    WORKPRODUCT("WorkProduct"),
    TYPE("Type"),
    METHOD("Method"),
    INPUT("Input"),
    EXPECTEDRESULT("ExpectedResult"),
    TESTCASE("TestCase"),
    VERDICT("Verdict"),
    DATE("Date"),
    BUILD("Build"),
    TESTER("Tester"),
    USERNAME("UserName"),
    EMAILADDRESS("EmailAddress");

    private String name;

    private Param(String name) {
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
