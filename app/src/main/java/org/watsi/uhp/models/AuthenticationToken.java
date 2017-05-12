package org.watsi.uhp.models;

import com.google.gson.annotations.SerializedName;

public class AuthenticationToken {

    @SerializedName("token")
    private String token;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("user")
    private User user;

    public AuthenticationToken() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
