package com.abdulkarim.tourmate.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abdulkarim.tourmate.R;


public class GroupFragment extends Fragment {

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

}
