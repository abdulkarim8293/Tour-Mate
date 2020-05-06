package com.abdulkarim.tourmate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.ActivityVerifyNumberBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyNumberActivity extends AppCompatActivity {

    private ActivityVerifyNumberBinding binding;
    private String mobileNumber;
    private String phoneNumberVerificationId;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_verify_number);

        init();

        if (getIntent().getExtras() != null) {
            mobileNumber = getIntent().getStringExtra("mobileNumber");
            sendVerificationCode(mobileNumber);
            binding.showNumberTV.setText(mobileNumber);
        }

        binding.backBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = binding.otpET.getText().toString();

                if (code.equals("") || code.length() < 6) {
                    binding.otpET.setError("Enter a valid code");
                    binding.otpET.requestFocus();
                } else {
                    verifyPhoneCode(code);
                }
            }
        });
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void sendVerificationCode(String mobileNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+88" + mobileNumber,
                120,
                TimeUnit.SECONDS,
                this,
                mCallbacks);

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {

            String mCode = credential.getSmsCode();
            if (mCode != null) {
                binding.otpET.setText(mCode);
                verifyPhoneCode(mCode);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {

            phoneNumberVerificationId = verificationId;

        }
    };

    private void verifyPhoneCode(String code) {

        binding.continueBtn.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneNumberVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(VerifyNumberActivity.this, UpdateProfileActivity.class);
                            intent.putExtra("mobileNumber", mobileNumber);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(VerifyNumberActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();
                            binding.continueBtn.setVisibility(View.VISIBLE);
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }

}
