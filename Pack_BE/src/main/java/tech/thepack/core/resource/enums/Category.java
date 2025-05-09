package tech.thepack.core.resource.enums;

public enum Category {
    LEADERSHIP("LEADERSHIP"),
    MANAGING_COMPLEXITY("MANAGING COMPLEXITY");

    private String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}