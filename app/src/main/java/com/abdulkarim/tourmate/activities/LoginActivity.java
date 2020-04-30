package com.abdulkarim.tourmate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.ActivityLoginBinding;
import com.abdulkarim.tourmate.helper.SharedPref;
import com.abdulkarim.tourmate.model.ResponseBody;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ApiService retrofitInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login);

        init();

        binding.createNewAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignUpActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobileNumber = binding.mobileNumberET.getText().toString();
                String password = binding.passwordET.getText().toString();

                if (mobileNumber.isEmpty() || mobileNumber.equals("")){
                    binding.mobileNumberET.setError("Enter a Email or Mobile number");
                    binding.mobileNumberET.requestFocus();
                }else if (password.isEmpty() || password.equals("")){
                    binding.passwordET.setError("Enter your password");
                    binding.passwordET.requestFocus();
                }else {
                    login(mobileNumber,password);
                }
            }
        });

        binding.backBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void login(String mobileNumber, String password) {
        binding.loginBtn.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        Call<ResponseBody> call = retrofitInstance.login(mobileNumber,password);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response!=null){
                    if (response.body().isStatus()==true){
                        editor.putInt(SharedPref.USER_ID, response.body().getMemberId());
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.loginBtn.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void init() {
        sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        retrofitInstance = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
    }
}
