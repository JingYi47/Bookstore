package com.thanhvan.bookstoremanager.model;

import java.io.Serializable;

public class OrderItem implements Serializable {

    private int id;
    private int orderId;
    private String productId;
    private String productName;
    private double productPrice;
    private int productQuantity;
    private String productImageUrl;
    private String productCategory;

    public OrderItem() {
    }

    public OrderItem(int id, int orderId, String productId, String productName, double productPrice, int productQuantity, String productImageUrl, String productCategory) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productImageUrl = productImageUrl;
        this.productCategory = productCategory;
    }

    public OrderItem(int orderId, String productId, String productName, double productPrice, int productQuantity, String productImageUrl, String productCategory) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productImageUrl = productImageUrl;
        this.productCategory = productCategory;
    }

    public OrderItem(String productId, String productName, double productPrice, int productQuantity, String productImageUrl, String productCategory) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productImageUrl = productImageUrl;
        this.productCategory = productCategory;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public int getProductQuantity() { return productQuantity; }
    public void setProductQuantity(int productQuantity) { this.productQuantity = productQuantity; }
    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
}
