package com.thanhvan.bookstoremanager.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String productTitle;
    private double productPrice;
    private int productQuantity;
    private String imageUrl;
    private String productCategory;
    private String productId;

    public CartItem() {
    }

    public CartItem(String productTitle, double productPrice, int productQuantity, String imageUrl, String productCategory, String productId) {
        this.productTitle = productTitle;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.imageUrl = imageUrl;
        this.productCategory = productCategory;
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productTitle='" + productTitle + '\'' +
                ", productPrice=" + productPrice +
                ", productQuantity=" + productQuantity +
                ", imageUrl='" + imageUrl + '\'' +
                ", productCategory='" + productCategory + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
