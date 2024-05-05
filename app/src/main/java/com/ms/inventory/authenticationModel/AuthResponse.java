
package com.ms.inventory.authenticationModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("ReturnData")
    @Expose
    private ReturnData returnData;
    @SerializedName("Message")
    @Expose
    private Object message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public ReturnData getReturnData() {
        return returnData;
    }

    public void setReturnData(ReturnData returnData) {
        this.returnData = returnData;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

}
