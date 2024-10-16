package com.example.newyogaapplication.classes;

import java.io.Serializable;

public class YogaUser implements Serializable {
    private String userId;
    private String firebaseKey;
    private String email;
    private String password;
    private String username;
    private Role role;

    // Constructor
    public YogaUser(String userId, String firebaseKey, String email, String password, String username, Role role) {
        this.userId = userId;
        this.firebaseKey = firebaseKey;
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
    }

    public YogaUser() {
        // Required no-arg constructor for Firebase or serialization
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Convert the Role enum to a string for easier Firebase integration
    public String getRoleAsString() {
        return role != null ? role.getRoleName() : null;
    }

    // Set role from a string value (for Firebase or database)
    public void setRoleFromString(String roleName) {
        this.role = Role.fromString(roleName);
    }

    @Override
    public String toString() {
        return "YogaUser{" +
                "userId='" + userId + '\'' +
                ", firebaseKey='" + firebaseKey + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
