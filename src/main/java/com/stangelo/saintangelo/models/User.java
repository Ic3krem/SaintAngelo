package com.stangelo.saintangelo.models;

import java.time.LocalDateTime;

public class User {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private UserRole role;
    private String permissions;
    private String status;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;

    public User(String id, String username, String password, String fullName, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = "Active";
    }

    public User(String id, String username, String password, String fullName, String email, 
                UserRole role, String permissions, String status, LocalDateTime lastActive, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.permissions = permissions;
        this.status = status;
        this.lastActive = lastActive;
        this.createdAt = createdAt;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

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
}
