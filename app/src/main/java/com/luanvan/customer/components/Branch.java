package com.luanvan.customer.components;

import com.google.android.gms.maps.model.LatLng;

public class Branch {
    private int id;
    private String name;
    private String phoneNumber;
    private String imageUrl;
    private String openingTime;
    private String closingTime;
    private String address;
    private LatLng latLng;
    private boolean isSell;
    private int merchant;
    private String branchStatus;

    public Branch() { }

    public Branch(int id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Branch(int id, String name, String phoneNumber, String imageUrl, String openingTime, String closingTime, String address, LatLng latLng, boolean isSell, int merchant, String branchStatus) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.imageUrl = imageUrl;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.address = address;
        this.latLng = latLng;
        this.isSell = isSell;
        this.merchant = merchant;
        this.branchStatus = branchStatus;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSell(boolean sell) {
        isSell = sell;
    }

    public void setMerchant(int merchant) {
        this.merchant = merchant;
    }

    public void setBranchStatus(String branchStatus) {
        this.branchStatus = branchStatus;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public String getAddress() {
        return address;
    }

    public boolean isSell() {
        return isSell;
    }

    public int getMerchant() {
        return merchant;
    }

    public String getBranchStatus() {
        return branchStatus;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
