package com.example.newyogaapplication.classes;

public enum Role {
    CUSTOMER("customer"),
    TEACHER("teacher"),
    ADMIN("admin");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static Role fromString(String roleName) {
        for (Role role : Role.values()) {
            if (role.roleName.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        return null; // Handle cases where roleName doesn't match
    }
}
