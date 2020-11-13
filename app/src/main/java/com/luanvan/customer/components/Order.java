package com.luanvan.customer.components;

import java.util.ArrayList;

public class Order {
    private int id;
    private double totalPay;
    private double victualsPrice;
    private double shippingFee;
    private String shippingAddress;
    private String note;
    private String time;
    private Branch branch;
    private int consumer;
    private int shipper;
    private String orderStatus;
    private ArrayList<CartItem> orderItems;

    public Order() { }

    public Order(int id, double totalPay, double victualsPrice, double shippingFee, String shippingAddress, String note, String time, Branch branch, int consumer, int shipper, String orderStatus, ArrayList<CartItem> orderItems) {
        this.id = id;
        this.totalPay = totalPay;
        this.victualsPrice = victualsPrice;
        this.shippingFee = shippingFee;
        this.shippingAddress = shippingAddress;
        this.note = note;
        this.time = time;
        this.branch = branch;
        this.consumer = consumer;
        this.shipper = shipper;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTotalPay(double totalPay) {
        this.totalPay = totalPay;
    }

    public void setVictualsPrice(double victualsPrice) {
        this.victualsPrice = victualsPrice;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public void setConsumer(int consumer) {
        this.consumer = consumer;
    }

    public void setShipper(int shipper) {
        this.shipper = shipper;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setOrderItems(ArrayList<CartItem> orderItems) {
        this.orderItems = orderItems;
    }

    public int getId() {
        return id;
    }

    public double getTotalPay() {
        return totalPay;
    }

    public double getVictualsPrice() {
        return victualsPrice;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getNote() {
        return note;
    }

    public String getTime() {
        return time;
    }

    public Branch getBranch() {
        return branch;
    }

    public int getConsumer() {
        return consumer;
    }

    public int getShipper() {
        return shipper;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public ArrayList<CartItem> getOrderItems() {
        return orderItems;
    }
}
