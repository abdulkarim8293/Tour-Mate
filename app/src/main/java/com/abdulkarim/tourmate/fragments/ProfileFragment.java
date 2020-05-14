package com.abdulkarim.tourmate.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.adapters.ViewPagerAdapter;
import com.abdulkarim.tourmate.databinding.FragmentProfileBinding;
import com.abdulkarim.tourmate.helper.CustomProgressDialog;
import com.abdulkarim.tourmate.helper.SharedPref;
import com.abdulkarim.tourmate.model.User;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ViewPagerAdapter adapter;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ApiService retrofitInstance;
    private ProgressDialog progressDialog;

    private int userId;
    private String imageUrl;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false);

        init();
        initTabLayout();

        progressDialog = CustomProgressDialog.createProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.isIndeterminate();
        progressDialog.show();

        binding.userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUrl!=null && !imageUrl.equals("")){
                    openImageZoomingDialog(imageUrl);
                }
            }
        });

        binding.swipeRefreshL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserInfo();
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
    }

    private void init() {
        sharedPreferences = getActivity().getSharedPreferences(SharedPref.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        retrofitInstance = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        userId = sharedPreferences.getInt(SharedPref.USER_ID,0);
    }

    private void getUserInfo() {
        Call<User> call = retrofitInstance.getUserInfo(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response!=null){

                    User user = response.body();
                    binding.swipeRefreshL.setRefreshing(false);
                    if (progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }

                    if (user.getPhoto()!=null && !user.getPhoto().equals("")){
                        Glide.with(getActivity().getApplicationContext()).applyDefaultRequestOptions(new RequestOptions()
                                .placeholder(R.drawable.my_profile_image)).load(RetrofitInstance.BASE_URL+user.getPhoto()).into(binding.userProfileImage);

                    }
                    if (user.getPhoto()!=null && !user.getPhoto().equals("")){
                        imageUrl = RetrofitInstance.BASE_URL+user.getPhoto();
                    }


                    if (user.isVerified() == true) {
                        binding.verifiedTV.setText("Verified");
                        for (Drawable drawable : binding.verifiedTV.getCompoundDrawables()) {
                            if (drawable != null) {
                                drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN));
                            }
                        }
                    } else {
                        binding.verifiedTV.setText("Not Verified");
                        for (Drawable drawable : binding.verifiedTV.getCompoundDrawables()) {
                            if (drawable != null) {
                                drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_IN));
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    private void initTabLayout() {
        adapter= new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new GeneralInfoFragment(),"General Info");
        adapter.addFragment(new IdentityFragment(),"Identity");

        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setSaveFromParentEnabled(false);
        binding.viewPager.setAdapter(adapter);

        binding.tabLayout.setupWithViewPager(binding.viewPager);
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
