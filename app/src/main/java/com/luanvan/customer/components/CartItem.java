package com.luanvan.customer.components;

public class CartItem {
    private int id;
    private String name;
    private String imageUrl;
    private double price;
    private double discount;
    private int quantity;
    private int cartId;
    private int victualsId;
    public CartItem(){}

    public CartItem(int id, String name, String imageUrl, double price, double discount, int quantity, int cartId, int victualsId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.discount = discount;
        this.quantity = quantity;
        this.cartId = cartId;
        this.victualsId = victualsId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public void setVictualsId(int victualsId) {
        this.victualsId = victualsId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getDiscount() {
        return discount;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCartId() {
        return cartId;
    }

    public int getVictualsId() {
        return victualsId;
    }
}
