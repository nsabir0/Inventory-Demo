package com.ms.inventory.model;

public class UserAuth {
    private String userName;
    private String userPassword;

    public UserAuth(String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
