package tech.thepack.core.resource.enums;

public enum Role {
    MENTOR_COACH("MENTOR/COACH"),
    MENTEE_COACHEE("MENTEE/COACHEE");

    private final String displayName;

    Role(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
