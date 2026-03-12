package com.kyas.wolkandhold.ui.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;

    private String jwtToken;

    public LoggedInUser(String userId, String displayName, String jwtToken) {
        this.userId = userId;
        this.displayName = displayName;
        this.jwtToken = jwtToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}