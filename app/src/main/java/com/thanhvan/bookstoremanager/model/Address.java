package com.thanhvan.bookstoremanager.model;

import java.io.Serializable;

public class Address implements Serializable {
    private int id;
    private String userEmail;
    private String name;
    private String phone;
    private String fullAddress;
    private double latitude;
    private double longitude;

    public Address() {
    }

    public Address(String userEmail, String name, String phone, String fullAddress, double latitude, double longitude) {
        this.userEmail = userEmail;
        this.name = name;
        this.phone = phone;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Address(int id, String userEmail, String name, String phone, String fullAddress, double latitude, double longitude) {
        this.id = id;
        this.userEmail = userEmail;
        this.name = name;
        this.phone = phone;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", userEmail='" + userEmail + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", fullAddress='" + fullAddress + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
