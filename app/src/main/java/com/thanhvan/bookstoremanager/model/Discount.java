package com.thanhvan.bookstoremanager.model;

public class Discount {
    private int id;
    private String name;
    private double discountPercentage;
    private double minOrderValue;
    private String description;
    private boolean isActive;

    public Discount(int id, String name, double discountPercentage, double minOrderValue, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.discountPercentage = discountPercentage;
        this.minOrderValue = minOrderValue;
        this.description = description;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}