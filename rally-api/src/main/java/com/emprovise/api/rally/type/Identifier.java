package com.emprovise.api.rally.type;

import java.util.Arrays;
import java.util.Optional;

public enum Identifier {

    FEATURE("F", "PortfolioItem/Feature", "feature", "features"),
    INITIATIVE("I", "PortfolioItem/Initiative", "initiative", "initiatives"),
    PARENT_FEATURE("P", "PortfolioItem/ParentFeature", "parentfeature", "parentfeatures"),
    USER_STORY("US", "HierarchicalRequirement", "userstory", "userstories"),
    DEFECT("DE", "Defect", "defect", "defects"),
    TASK("TA", "Task", "task", "tasks"),
    TESTCASE("TC", "TestCase", "testcase", "testcases");

    private String prefix;
    private String objectType;
    private String value;
    private String plural;

    Identifier(String prefix, String objectType, String value, String plural) {
        this.prefix = prefix;
        this.objectType = objectType;
        this.value = value;
        this.plural = plural;
    }

    public String prefix() {
        return this.prefix;
    }

    public String objectType() {
        return this.objectType;
    }

    public String value() {
        return this.value;
    }

    public String plural() {
        return this.plural;
    }

    public static Identifier getIdentifierByPrefix(final String prefix) {

        Optional<Identifier> rallyIdentifier = Arrays.stream(Identifier.values())
                                                        .filter(identifier -> identifier.prefix().equals(prefix))
                                                        .findAny();
        return rallyIdentifier.orElse(null);
    }

    public static Identifier getIdentifierByValue(final String value) {

        Optional<Identifier> rallyIdentifier = Arrays.stream(Identifier.values())
                                                        .filter(identifier -> identifier.value().equals(value))
                                                        .findAny();
        return rallyIdentifier.orElse(null);
    }

    public static Identifier getIdentifierByPlural(final String plural) {

        Optional<Identifier> rallyIdentifier = Arrays.stream(Identifier.values())
                                                        .filter(identifier -> identifier.plural().equals(plural))
                                                        .findAny();
        return rallyIdentifier.orElse(null);
    }
}
