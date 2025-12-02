package com.stangelo.saintangelo.services;

import com.stangelo.saintangelo.models.User;

/**
 * Authentication Service
 * Manages user authentication and session management
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class AuthService {

    private static AuthService instance;
    private User currentUser;

    /**
     * Private constructor for singleton pattern
     */
    private AuthService() {
        this.currentUser = null;
    }

    /**
     * Gets the singleton instance
     *
     * @return AuthService instance
     */
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Sets the current logged-in user
     *
     * @param user User object
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the current logged-in user
     *
     * @return Current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in
     *
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Logs out the current user
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Checks if current user has admin privileges
     *
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}

