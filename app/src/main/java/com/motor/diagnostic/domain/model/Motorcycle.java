package com.motor.diagnostic.domain.model;

import java.util.Date;

/**
 * Domain model for Motorcycle entity
 */
public class Motorcycle {
    private String id;
    private String userId;
    private String nickname;
    private String make;
    private String model;
    private String imageUrl;
    private Date registrationDate;
    private boolean isConnected;
    private String deviceId;
    private int year;
    
    // Default constructor required for Firebase
    public Motorcycle() {}
    
    public Motorcycle(String id, String userId, String nickname, String make, String model, 
                      String imageUrl, Date registrationDate, boolean isConnected, String deviceId, int year) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.make = make;
        this.model = model;
        this.imageUrl = imageUrl;
        this.registrationDate = registrationDate;
        this.isConnected = isConnected;
        this.deviceId = deviceId;
        this.year = year;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
} 