package com.abdulkarim.tourmate.bottom_sheet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.BottomSheetUserInfoBinding;
import com.abdulkarim.tourmate.fragments.ImageZoomingDialog;
import com.abdulkarim.tourmate.model.MemberLocation;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserInfoBottomSheet extends BottomSheetDialogFragment {

    private String selectedUserID;
    private BottomSheetUserInfoBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private String mobileNumber;

    private String imageUrl;

    public UserInfoBottomSheet() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_user_info, container, false);

        if (getArguments() != null) {
            selectedUserID = getArguments().getString("selectedUserID");
        } else {
            selectedUserID = "";
        }

        init();

        getUserData();

        binding.callBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mobileNumber != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + mobileNumber));
                    getActivity().startActivity(intent);
                }
            }
        });


        binding.profileImageCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUrl!=null && !imageUrl.equals("")){
                    openImageZoomingDialog(imageUrl);
                }
            }
        });

        return binding.getRoot();
    }

    private void getUserData() {
        DatabaseReference userDB = firebaseDatabase.getReference().child("users").child(selectedUserID);
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("name").exists() && dataSnapshot.child("latitude").exists()) {
                        MemberLocation userInfo = dataSnapshot.getValue(MemberLocation.class);

                        if (userInfo.getPhoto() != null && !userInfo.getPhoto().equals("")) {
                            Glide.with(getActivity().getApplicationContext()).applyDefaultRequestOptions(new RequestOptions()
                                    .placeholder(R.drawable.my_profile_image)).load(RetrofitInstance.BASE_URL + userInfo.getPhoto()).into(binding.profileImageCIV);
                        }

                        if (userInfo.getPhoto() != null && !userInfo.getPhoto().equals("")) {
                            imageUrl = RetrofitInstance.BASE_URL + userInfo.getPhoto();
                        }

                        binding.nameTV.setText(userInfo.getName());
                        binding.mobileNumberTV.setText(userInfo.getPhone());
                        mobileNumber = userInfo.getPhone();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void openImageZoomingDialog(String imageUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", imageUrl);
        ImageZoomingDialog dialog = new ImageZoomingDialog();
        dialog.setArguments(bundle);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        dialog.show(ft, "TAG");
    }

}
