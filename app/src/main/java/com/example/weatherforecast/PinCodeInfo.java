package com.example.weatherforecast;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PinCodeInfo {

    @SerializedName("Message")
    public String message;
    @SerializedName("Status")
    public String status;
    @SerializedName("PostOffice")
    public List<PostOffice> postOffice;

    public PinCodeInfo(String message, String status, List<PostOffice> postOffice) {
        this.message = message;
        this.status = status;
        this.postOffice = postOffice;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public List<PostOffice> getPostOffice() {
        return postOffice;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPostOffice(List<PostOffice> postOffice) {
        this.postOffice = postOffice;
    }
}
