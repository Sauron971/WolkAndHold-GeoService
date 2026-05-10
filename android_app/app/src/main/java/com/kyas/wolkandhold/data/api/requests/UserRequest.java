package com.kyas.wolkandhold.data.api.requests;

public class UserRequest {
    private long id;
    private String username;

    public UserRequest(long id, String username) {
        this.id = id;
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserRequest{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
