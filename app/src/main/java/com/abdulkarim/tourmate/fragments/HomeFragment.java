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
import com.abdulkarim.tourmate.helper.CustomProgressDialog;
import com.abdulkarim.tourmate.helper.SharedPref;
import com.abdulkarim.tourmate.helper.StaticKeys;
import com.abdulkarim.tourmate.model.MemberLocation;
import com.abdulkarim.tourmate.model.User;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

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

        init();
        getUserInfo();
        getLocationPermission();

        progressDialog = CustomProgressDialog.createProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.isIndeterminate();
        progressDialog.show();

        binding.myLocationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        binding.captainLocationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (captainLatLng != null) {
                    if (groupMemberDB != null) {
                        groupMemberDB.removeEventListener(valueEventListener);

                    }

                    if (map != null) {
                        map.clear();
                    }

                    setCaptainMarker(captainLatLng);
                    animateCamera(captainLatLng, DEFAULT_ZOOM);
                    binding.clearFAB.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.hotelLocationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hotelLatLng != null) {
                    if (groupMemberDB != null) {
                        groupMemberDB.removeEventListener(valueEventListener);

                    }

                    if (map != null) {
                        map.clear();
                    }

                    setHomeMarker(hotelLatLng);

                    animateCamera(hotelLatLng, DEFAULT_ZOOM);
                    binding.clearFAB.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.clearFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null && groupMemberDB != null && valueEventListener != null) {
                    map.clear();
                    groupMemberDB.addValueEventListener(valueEventListener);
                    if (currentLocation != null) {
                        animateCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                    }
                    binding.clearFAB.setVisibility(View.GONE);
                } else {

                    if (map != null) {
                        map.clear();
                    }
                    binding.clearFAB.setVisibility(View.GONE);
                }
            }
        });

        return binding.getRoot();
    }

    private void init() {
        memberLocationList = new ArrayList<>();
        fragmentManager = getActivity().getSupportFragmentManager();
        firebaseDatabase = FirebaseDatabase.getInstance();
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
                    User user = response.body();

                    editor.putString(SharedPref.MEMBER_TYPE, user.getMemberType());
                    editor.apply();

                    binding.hotelLocationFAB.setLabelText(user.getTourDetails());
                    hotelLatLng = new LatLng(user.getDestinationLat(), user.getDestinationLong());

                    updateUserInfoToDB(user);
                    getPermission(user);
                    getCaptainId(user.getGroupId());

                    if (user.getMemberType().equals(StaticKeys.CAPTAIN)) {
                        binding.captainLocationFAB.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionsGranted = true;
                    initializeMap();
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            permissions,
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            locationPermissionsGranted = true;
            initializeMap();
        }

    }

    private void getDeviceLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        visibleMyLocationBtn = false;
        try {
            if (locationPermissionsGranted) {

                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            currentLocation = (Location) task.getResult();

                            animateCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                            updateLocationToDB(currentLocation);

                            startLocationService();
                        } else {
                            Toast.makeText(getActivity(), "Unable to access your current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
        }
    }

}
