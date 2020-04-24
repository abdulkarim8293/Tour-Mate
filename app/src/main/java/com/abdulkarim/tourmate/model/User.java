package com.abdulkarim.tourmate.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("user_id")
    private int userId;
    private String name;
    private String address;
    private String phone;
    private String photo;
    private String gender;
    private String email;

    @SerializedName("group_id")
    private int groupId;

    @SerializedName("group_name")
    private String groupName;

    @SerializedName("tour_details")
    private String tourDetails;

    @SerializedName("destination_lat")
    private double destinationLat;

    @SerializedName("destination_long")
    private double destinationLong;

    @SerializedName("cover_redius")
    private int coverRedius;

    @SerializedName("member_type")
    private String memberType;

    @SerializedName("is_verified")
    private boolean isVerified;

    public User() {
    }

    public User(int userId, String name, String address, String phone, String photo, String gender, String email, int groupId, String groupName,
                String tourDetails, double destinationLat, double destinationLong, int coverRedius, String memberType, boolean isVerified) {
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.photo = photo;
        this.gender = gender;
        this.email = email;
        this.groupId = groupId;
        this.groupName = groupName;
        this.tourDetails = tourDetails;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.coverRedius = coverRedius;
        this.memberType = memberType;
        this.isVerified = isVerified;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhoto() {
        return photo;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getTourDetails() {
        return tourDetails;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public double getDestinationLong() {
        return destinationLong;
    }

    public int getCoverRedius() {
        return coverRedius;
    }

    public String getMemberType() {
        return memberType;
    }

    public boolean isVerified() {
        return isVerified;
    }
}
