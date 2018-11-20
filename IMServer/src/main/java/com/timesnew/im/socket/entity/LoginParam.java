package com.timesnew.im.socket.entity;

import java.io.Serializable;
import java.util.Objects;

public class LoginParam implements Serializable {
    private static final long serialVersionUID = 7801069267115619312L;
    private String platform;
    private String userId;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "LoginParam{" +
                "platform='" + platform + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginParam that = (LoginParam) o;
        return Objects.equals(platform, that.platform) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, userId);
    }
}
