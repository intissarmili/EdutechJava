package service;

import models.User;

public class AuthService {
    private static AuthService instance;
    private User currentUser;

    private AuthService() {
        // Constructeur privé pour le pattern Singleton
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }
} 