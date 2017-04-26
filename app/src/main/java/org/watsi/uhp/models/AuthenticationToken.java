package org.watsi.uhp.models;

import com.google.gson.annotations.SerializedName;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.managers.Clock;

import java.text.ParseException;
import java.util.Date;

public class AuthenticationToken {

    public static int REFRESH_TOKEN_THRESHOLD = 1000 * 60 * 60 * 24 * 7; // 7 days in milliseconds

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

    public long timeTillExpiration() {
        try {
            Date expiresAtDate = Clock.ISO_DATE_FORMAT.parse(getExpiresAt());
            return expiresAtDate.getTime() - Clock.getCurrentTime().getTime();
        } catch (ParseException e) {
            Rollbar.reportException(e);
            return -1;
        }
    }

    public boolean shouldRefreshToken() {
        return timeTillExpiration() <= REFRESH_TOKEN_THRESHOLD;
    }
}
