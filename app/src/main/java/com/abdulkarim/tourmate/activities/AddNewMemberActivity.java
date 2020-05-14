package com.abdulkarim.tourmate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.ActivityAddNewMemberBinding;

public class AddNewMemberActivity extends AppCompatActivity {

    private ActivityAddNewMemberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = DataBindingUtil.setContentView(this,R.layout.activity_add_new_member);

        binding.backBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}
