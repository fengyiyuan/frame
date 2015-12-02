package com.feng.arch.sdk.po;

import com.feng.core.po.BasePO;

public class FengUser extends BasePO {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 年龄
     */
    private Integer currAge;

    /**
     * 状态 1 正常 0 禁用
     */
    private Byte userStatus;

    private String mobileNum;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public Integer getCurrAge() {
        return currAge;
    }

    public void setCurrAge(Integer currAge) {
        this.currAge = currAge;
    }

    public Byte getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Byte userStatus) {
        this.userStatus = userStatus;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum == null ? null : mobileNum.trim();
    }
}