package com.luanvan.customer.components;

public class Category {
    private int id;
    private String name;
    private String imageUrl;

    public Category() { }
    public Category(int id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl(){
        return imageUrl;
    }
}
