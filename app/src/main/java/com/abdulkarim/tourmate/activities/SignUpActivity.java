package com.abdulkarim.tourmate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.ActivitySignUpBinding;
import com.abdulkarim.tourmate.model.ResponseBody;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up);

        getLocationPermission();

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.nextBtn.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.VISIBLE);

                String mobileNumber = binding.phoneNumberEtID.getText().toString();
                if (!mobileNumber.isEmpty() && mobileNumber.matches("01[0-9]{9}")) {

                    checkNumber(mobileNumber);

                } else {
                    binding.phoneNumberEtID.setError("Enter your valid phone number");
                    binding.phoneNumberEtID.requestFocus();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.nextBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.termsAndConditionCBID.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked==true){
                    binding.nextBtn.setEnabled(true);
                    binding.nextBtn.setBackground(getResources().getDrawable(R.drawable.all_side_border));

                }else if (isChecked==false){
                    binding.nextBtn.setEnabled(false);
                    binding.nextBtn.setBackground(getResources().getDrawable(R.drawable.all_side_border_gray));
                }
            }
        });

        binding.loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
            }
        });

    }

    private void checkNumber(final String mobileNumber) {

        ApiService retrofitInstance = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        Call<ResponseBody> call = retrofitInstance.matchNumber(mobileNumber);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.body().isStatus()==true){
                    binding.nextBtn.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                }else {
                    binding.nextBtn.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    startNextActivity(mobileNumber);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });


    }

    private void startNextActivity(String mobileNumber) {
        startActivity(new Intent(SignUpActivity.this,VerifyNumberActivity.class)
                .putExtra("mobileNumber",mobileNumber));
    }


    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            permissions,
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }

    }
}
