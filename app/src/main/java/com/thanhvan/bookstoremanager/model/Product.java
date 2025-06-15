package com.thanhvan.bookstoremanager.model;

public class Product {
    private String id;
    private String title;
    private String author;
    private String category;
    private double rating;
    private double price;
    private double originalPrice;
    private int imageResId;

    public Product() { }

    public Product(String id, String title, String author, String category, double rating, double price, double originalPrice, int imageResId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.rating = rating;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imageResId = imageResId;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public double getRating() { return rating; }
    public double getPrice() { return price; }
    public double getOriginalPrice() { return originalPrice; }
    public int getImageResId() { return imageResId; }

    // Setters (Nếu cần, có thể dùng Alt+Insert để generate)
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(String category) { this.category = category; }
    public void setRating(double rating) { this.rating = rating; }
    public void setPrice(double price) { this.price = price; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

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
                ", imageResId=" + imageResId +
                '}';
    }
}