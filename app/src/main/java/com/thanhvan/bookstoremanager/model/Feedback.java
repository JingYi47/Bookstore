package com.thanhvan.bookstoremanager.model;

import java.io.Serializable;

public class Feedback implements Serializable {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String content;
    private long timestamp;

    public Feedback() {
    }

    public Feedback(String name, String phone, String email, String content, long timestamp) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Feedback(int id, String name, String phone, String email, String content, long timestamp) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}