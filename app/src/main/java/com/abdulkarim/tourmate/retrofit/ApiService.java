package com.abdulkarim.tourmate.retrofit;

import com.abdulkarim.tourmate.model.Group;
import com.abdulkarim.tourmate.model.GroupMember;
import com.abdulkarim.tourmate.model.ResponseBody;
import com.abdulkarim.tourmate.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface ApiService {

    @FormUrlEncoded
    @POST("api/number-match")
    Call<ResponseBody> matchNumber(@Field("phone") String mobileNumber);


    @GET("api/groups")
    Call<List<Group>> getGroups();


    @FormUrlEncoded
    @POST("api/signup")
    Call<ResponseBody> registerUser(
            @Field("phone") String mobileNumber
            , @Field("name") String name
            , @Field("gender") String gender
            , @Field("group_id") int groupId
            , @Field("photo") String photo);

    @GET("api/user-profile/{id}")
    Call<User> getUserInfo(@Path("id") int id);

    @GET("api/member-permission/{id}")
    Call<ResponseBody> getPermission(@Path("id") int id);


    @GET("api/group-member-list/{id}")
    Call<List<GroupMember>> getMembers(@Path("id") int groupId);


    @FormUrlEncoded
    @POST("api/member-permission-update")
    Call<ResponseBody> savePermission(
            @Field("member_id") int memberId
            , @Field("permission_status") String permissionStatus
            , @Field("captain_id") int captainId);


    @FormUrlEncoded
    @POST("api/profile_update/{id}")
    Call<ResponseBody> updateProfile(@Path("id") int id
            , @Field("name") String name
            , @Field("email") String email
            , @Field("gender") String gender
            , @Field("photo") String photo
            , @Field("address") String address
            , @Field("phone") String phone);


    @GET("api/get-captain-id/{id}")
    Call<ResponseBody> getCaptainId(@Path("id") int id);


    @FormUrlEncoded
    @POST("api/password-change-method-hrsoftbd")
    Call<ResponseBody> updatePassword(
            @Field("user_id") int user_id
            , @Field("password") String password);


    @FormUrlEncoded
    @POST("api/login")
    Call<ResponseBody> login(
            @Field("email_or_phone") String EmailOrPhone
            , @Field("password") String password);



}
