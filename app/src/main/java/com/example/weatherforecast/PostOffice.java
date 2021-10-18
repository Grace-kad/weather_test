package com.example.weatherforecast;

import com.google.gson.annotations.SerializedName;

public class PostOffice {

    @SerializedName("Name")
    public String name;
    @SerializedName("Description")
    public String description;
    @SerializedName("BranchType")
    public String branchType;
    @SerializedName("DeliveryStatus")
    public String deliveryStatus;
    @SerializedName("Circle")
    public String circle;
    @SerializedName("District")
    public String district;
    @SerializedName("Division")
    public String division;
    @SerializedName("Region")
    public String region;
    @SerializedName("Block")
    public String block;
    @SerializedName("State")
    public String state;
    @SerializedName("Country")
    public String country;
    @SerializedName("Pincode")
    public String pincode;

    public PostOffice(String name, String description, String branchType, String deliveryStatus, String circle, String district, String division, String region, String block, String state, String country, String pincode) {
        this.name = name;
        this.description = description;
        this.branchType = branchType;
        this.deliveryStatus = deliveryStatus;
        this.circle = circle;
        this.district = district;
        this.division = division;
        this.region = region;
        this.block = block;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBranchType() {
        return branchType;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getCircle() {
        return circle;
    }

    public String getDistrict() {
        return district;
    }

    public String getDivision() {
        return division;
    }

    public String getRegion() {
        return region;
    }

    public String getBlock() {
        return block;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPincode() {
        return pincode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBranchType(String branchType) {
        this.branchType = branchType;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
}
