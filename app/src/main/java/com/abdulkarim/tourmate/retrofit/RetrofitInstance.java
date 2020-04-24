package com.abdulkarim.tourmate.retrofit;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    public static String BASE_URL="http://tourmate.mpapp.info/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(){

        if (retrofit==null){
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
