package com.abdulkarim.tourmate.fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.FragmentHomeBinding;
import com.abdulkarim.tourmate.model.MemberLocation;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final Float DEFAULT_ZOOM = 17.0f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FirebaseDatabase firebaseDatabase;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ApiService retrofitInstance;

    private FragmentHomeBinding binding;
    private FragmentManager fragmentManager;
    private GoogleMapOptions googleMapOptions;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ProgressDialog progressDialog;

    private Boolean locationPermissionsGranted = false;
    private boolean visibleMyLocationBtn = false;
    private int userId;
    private int groupId;
    private LatLng captainLatLng;
    private LatLng hotelLatLng;
    private Location currentLocation;
    private List<MemberLocation> memberLocationList;

    private ValueEventListener valueEventListener;
    private Query groupMemberDB;

    public HomeFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        return binding.getRoot();
    }

}
