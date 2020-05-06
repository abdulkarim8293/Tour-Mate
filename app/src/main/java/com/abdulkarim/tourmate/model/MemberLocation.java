package com.abdulkarim.tourmate.model;

public class MemberLocation {

    private int groupId;
    private double latitude;
    private double longitude;
    private String name;
    private String phone;
    private String photo;
    private int userId;

    public MemberLocation() {

    }

    public MemberLocation(int groupId, double latitude, double longitude, String name, String phone, String photo, int userId) {
        this.groupId = groupId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.phone = phone;
        this.photo = photo;
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhoto() {
        return photo;
    }

    public int getUserId() {
        return userId;
    }
}
