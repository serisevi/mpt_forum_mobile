package com.example.forummpt.dto;

import java.sql.Date;

public class GlobalBanDTO {

    private long id;
    private boolean loginBan;
    private boolean writeBan;
    private Date banStartDate;
    private Date banExpireDate;
    private Long userId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isLoginBan() {
        return loginBan;
    }

    public void setLoginBan(boolean loginBan) {
        this.loginBan = loginBan;
    }

    public boolean isWriteBan() {
        return writeBan;
    }

    public void setWriteBan(boolean writeBan) {
        this.writeBan = writeBan;
    }

    public Date getBanStartDate() {
        return banStartDate;
    }

    public void setBanStartDate(Date banStartDate) {
        this.banStartDate = banStartDate;
    }

    public Date getBanExpireDate() {
        return banExpireDate;
    }

    public void setBanExpireDate(Date banExpireDate) {
        this.banExpireDate = banExpireDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
