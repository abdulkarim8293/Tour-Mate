package com.abdulkarim.tourmate.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseBody {
    private boolean status;
    private String message;

    @SerializedName("group_member_id")
    @Expose
    private int memberId;

    @SerializedName("member_roll")
    @Expose
    private String memberRoll;


    @SerializedName("view_group_members")
    @Expose
    private boolean canViewGroupMembers;


    @SerializedName("captain_id")
    @Expose
    private int captainId;



    public ResponseBody() {
    }

    public ResponseBody(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public ResponseBody(boolean status, String message, int memberId, String memberRoll) {
        this.status = status;
        this.message = message;
        this.memberId = memberId;
        this.memberRoll = memberRoll;
    }

    public ResponseBody(boolean canViewGroupMembers) {
        this.canViewGroupMembers = canViewGroupMembers;
    }

    public ResponseBody(int captainId) {
        this.captainId = captainId;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getMemberRoll() {
        return memberRoll;
    }

    public boolean isCanViewGroupMembers() {
        return canViewGroupMembers;
    }

    public int getCaptainId() {
        return captainId;
    }
}
