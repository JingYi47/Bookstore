package com.thanhvan.bookstoremanager.model;

import java.io.Serializable;

public class Product implements Serializable {
    private String id;
    private String title;
    private String author;
    private String category;
    private double rating;
    private double price;
    private double originalPrice;
    private String imageUrl;

    public Product() { }

    public Product(String id, String title, String author, String category, double rating, double price, double originalPrice, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.rating = rating;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", rating=" + rating +
                ", price=" + price +
                ", originalPrice=" + originalPrice +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}