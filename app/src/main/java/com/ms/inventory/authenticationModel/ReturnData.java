
package com.ms.inventory.authenticationModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReturnData {

    @SerializedName("UserId")
    @Expose
    private String userId;
    @SerializedName("Password")
    @Expose
    private Object password;
    @SerializedName("FullName")
    @Expose
    private String fullName;
    @SerializedName("Address")
    @Expose
    private String address;
    @SerializedName("Phone")
    @Expose
    private String phone;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Object getPassword() {
        return password;
    }

    public void setPassword(Object password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
