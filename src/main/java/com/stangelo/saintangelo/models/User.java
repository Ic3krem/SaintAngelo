package com.stangelo.saintangelo.models;

public class User {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private UserRole role;

    public User(String id, String username, String password, String fullName, UserRole role)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {return username; }
    public String getPassword () {return password; }

    public UserRole getRole() {return role; }
    public void setRole(UserRole role) {this.role = role; }

    public String getFullName () {return fullName; }

    public boolean isAdmin() {
        return role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN;
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
};
