package com.abdulkarim.tourmate.fragments;


import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.adapters.GroupMemberAdapter;
import com.abdulkarim.tourmate.databinding.FragmentGroupBinding;
import com.abdulkarim.tourmate.helper.CustomProgressDialog;
import com.abdulkarim.tourmate.helper.SharedPref;
import com.abdulkarim.tourmate.helper.StaticKeys;
import com.abdulkarim.tourmate.model.GroupMember;
import com.abdulkarim.tourmate.model.User;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


public class GroupFragment extends Fragment implements GroupMemberAdapter.OnPermissionSave {

    private FragmentGroupBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ApiService retrofitInstance;
    private GroupMemberAdapter groupMemberAdapter;
    private ProgressDialog progressDialog;

    private int userId;
    private String memberType;
    private int groupId;

    private List<GroupMember> memberList;

    public GroupFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_group, container, false);

        init();

        progressDialog = CustomProgressDialog.createProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.isIndeterminate();
        progressDialog.show();

        initRecyclerView();
        getUserInfo();

        if (memberType.equals(StaticKeys.MEMBER)) {
            binding.addNewMemberTV.setVisibility(View.GONE);
            binding.groupMemberL.setVisibility(View.GONE);

        } else if (memberType.equals(StaticKeys.CAPTAIN)) {
            binding.addNewMemberTV.setVisibility(View.GONE);
            binding.groupMemberL.setVisibility(View.VISIBLE);
            binding.errorMessageTV.setVisibility(View.GONE);
        }


        binding.addNewMemberTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(getActivity(), AddNewMemberActivity.class),
                            ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(new Intent(getActivity(), AddNewMemberActivity.class));
                }
            }
        });

        binding.swipeRefreshL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (groupId > 0) {
                    getUserInfo();
                }
            }
        });

        return binding.getRoot();
    }

    private void getGroupMemberList(int groupId) {
        memberList.clear();

        Call<List<GroupMember>> call = retrofitInstance.getMembers(groupId);

        call.enqueue(new Callback<List<GroupMember>>() {
            @Override
            public void onResponse(Call<List<GroupMember>> call, Response<List<GroupMember>> response) {
                if (response != null) {
                    memberList.addAll(response.body());
                    groupMemberAdapter.notifyDataSetChanged();
                    binding.swipeRefreshL.setRefreshing(false);
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GroupMember>> call, Throwable t) {

            }
        });

    }

    private void initRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupMemberAdapter = new GroupMemberAdapter(memberList, getActivity(), this);

        binding.recyclerView.setAdapter(groupMemberAdapter);

    }


    private void init() {
        memberList = new ArrayList<>();

        sharedPreferences = getActivity().getSharedPreferences(SharedPref.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        retrofitInstance = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        userId = sharedPreferences.getInt(SharedPref.USER_ID, 0);
        memberType = sharedPreferences.getString(SharedPref.MEMBER_TYPE, "");

    }


    private void getUserInfo() {
        Call<User> call = retrofitInstance.getUserInfo(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response != null) {
                    User user = response.body();
                    binding.groupNameTV.setText(user.getGroupName());
                    groupId = user.getGroupId();

                    if (user.getMemberType().equals(StaticKeys.CAPTAIN)) {
                        getGroupMemberList(user.getGroupId());
                        binding.groupMemberL.setVisibility(View.VISIBLE);
                        binding.errorMessageTV.setVisibility(View.GONE);
                    } else if (user.getMemberType().equals(StaticKeys.MEMBER)
                            && sharedPreferences.getBoolean(SharedPref.PERMISSION, false) == true) {
                        getGroupMemberList(user.getGroupId());
                        binding.groupMemberL.setVisibility(View.VISIBLE);
                        binding.errorMessageTV.setVisibility(View.GONE);
                    } else {
                        binding.errorMessageTV.setVisibility(View.VISIBLE);
                        binding.swipeRefreshL.setRefreshing(false);
                        binding.groupMemberL.setVisibility(View.GONE);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }


    @Override
    public void onSuccess(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        if (groupId > 0) {
            getGroupMemberList(groupId);
        }
    }

}
