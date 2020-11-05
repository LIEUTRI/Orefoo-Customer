package com.luanvan.customer.components;

import java.util.ArrayList;

public class Victual {
    private String id;
    private String name;
    private String price;
    private String discountPrice;
    private String createdAt;
    private String updatedAt;
    private String imageUrl;
    private Boolean isSell;
    private String branch;
    private int quantity;
    private int cartItemId;
    private ArrayList<Integer> victualsCategories;
    public Victual(){ }
    public Victual(String id){
        this.id = id;
    }
    public Victual(String id, String name, String price, String discountPrice, String createdAt, String updatedAt, String imageUrl, Boolean isSell, String branch, ArrayList<Integer> victualsCategories, int quantity, int cartItemId){
        this.id = id;
        this.name = name;
        this.price = price;
        this.discountPrice = discountPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.imageUrl = imageUrl;
        this.isSell = isSell;
        this.branch = branch;
        this.victualsCategories = victualsCategories;
        this.quantity = quantity;
        this.cartItemId = cartItemId;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDiscountPrice() {
        return discountPrice;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Boolean getSell() {
        return isSell;
    }

    public String getBranch() {
        return branch;
    }

    public ArrayList<Integer> getVictualsCategories() {
        return victualsCategories;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDiscountPrice(String discountPrice) {
        this.discountPrice = discountPrice;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSell(Boolean sell) {
        isSell = sell;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setVictualsCategories(ArrayList<Integer> victualsCategories) {
        this.victualsCategories = victualsCategories;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }
}
