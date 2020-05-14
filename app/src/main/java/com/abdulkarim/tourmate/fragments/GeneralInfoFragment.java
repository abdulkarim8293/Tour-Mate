package com.abdulkarim.tourmate.fragments;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.activities.UpdateProfileActivity;
import com.abdulkarim.tourmate.databinding.FragmentGeneralInfoBinding;
import com.abdulkarim.tourmate.helper.SharedPref;
import com.abdulkarim.tourmate.model.ResponseBody;
import com.abdulkarim.tourmate.model.User;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class GeneralInfoFragment extends Fragment {

    private FragmentGeneralInfoBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ApiService retrofitInstance;
    private Dialog dialog;

    private int userId;
    private User user;

    public GeneralInfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_general_info, container, false);

        init();

        binding.editIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    startActivity(new Intent(getActivity(), UpdateProfileActivity.class)
                            .putExtra("mobileNumber", user.getPhone())
                            .putExtra("isForUpdate", true)
                            .putExtra("name", user.getName())
                            .putExtra("email", user.getEmail())
                            .putExtra("photo", user.getPhoto())
                            .putExtra("gender", user.getGender())
                            .putExtra("groupId", user.getGroupId())
                            .putExtra("address", user.getAddress()));
                }
            }
        });

        binding.changePasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContentView(R.layout.dialog_change_password);

                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView cancelBtn = dialog.findViewById(R.id.cancelBtnTV);
                TextView saveBtn = dialog.findViewById(R.id.saveBtnTV);
                final EditText passwordET = dialog.findViewById(R.id.passwordET);

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        hideSoftKeyboard();
                    }
                });
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password = passwordET.getText().toString();
                        if (password.equals("") || password.isEmpty()) {
                            Toast.makeText(getContext(), "Please enter a password", Toast.LENGTH_SHORT).show();
                        } else {
                            savePassword(password);
                        }
                    }
                });
                dialog.show();
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

        userId = sharedPreferences.getInt(SharedPref.USER_ID, 0);
    }

    private void getUserInfo() {
        Call<User> call = retrofitInstance.getUserInfo(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response != null) {
                    user = response.body();

                    binding.nameTV.setText(user.getName());
                    binding.mobileNumberTV.setText(user.getPhone());
                    binding.genderTV.setText(user.getGender());

                    if (user.getEmail() != null && !user.getEmail().equals("")) {
                        binding.emailTV.setText(user.getEmail());
                    }
                    if (user.getAddress() != null && !user.getAddress().equals("")) {
                        binding.addressTV.setText(user.getAddress());
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    private void savePassword(String password) {

        Call<ResponseBody> call = retrofitInstance.updatePassword(sharedPreferences.getInt(SharedPref.USER_ID, 0), password);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    if (response.body().isStatus()==true){
                        if (dialog!=null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void hideSoftKeyboard() {
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
