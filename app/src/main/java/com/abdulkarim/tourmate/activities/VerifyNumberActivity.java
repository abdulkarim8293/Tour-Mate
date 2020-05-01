package com.abdulkarim.tourmate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.ActivityVerifyNumberBinding;

public class VerifyNumberActivity extends AppCompatActivity {

    private ActivityVerifyNumberBinding binding;
    private String mobileNumber;
    private String phoneNumberVerificationId;

    //private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_number);
    }
}
