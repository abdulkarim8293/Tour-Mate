package com.abdulkarim.tourmate.model;

import com.google.gson.annotations.SerializedName;

public class GroupMember {

    private String name;
    private String phone;
    private String photo;

    @SerializedName("group_id")
    private int groupId;

    @SerializedName("member_type")
    private String memberType;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("view_group_members")
    private boolean canViewGroupMembers;

    public GroupMember() {
    }


    public GroupMember(String name, String phone, String photo, int groupId, String memberType, int userId, boolean canViewGroupMembers) {
        this.name = name;
        this.phone = phone;
        this.photo = photo;
        this.groupId = groupId;
        this.memberType = memberType;
        this.userId = userId;
        this.canViewGroupMembers = canViewGroupMembers;
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

    public int getGroupId() {
        return groupId;
    }

    public String getMemberType() {
        return memberType;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isCanViewGroupMembers() {
        return canViewGroupMembers;
    }
}
