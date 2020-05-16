package com.abdulkarim.tourmate.fragments;


import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.bottom_sheet.UserInfoBottomSheet;
import com.abdulkarim.tourmate.databinding.FragmentHomeBinding;
import com.abdulkarim.tourmate.helper.CustomProgressDialog;
import com.abdulkarim.tourmate.helper.SharedPref;
import com.abdulkarim.tourmate.helper.StaticKeys;
import com.abdulkarim.tourmate.model.MemberLocation;
import com.abdulkarim.tourmate.model.ResponseBody;
import com.abdulkarim.tourmate.model.User;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;
import com.abdulkarim.tourmate.service.LocationService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements OnMapReadyCallback, SearchView.OnQueryTextListener{

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

    private void setHomeMarker(LatLng latLng) {
        map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_icon)));
    }

    private void setCaptainMarker(LatLng latLng) {
        map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.captain_icon)));
    }

    private void animateCamera(LatLng latLng, float zoom) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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

    private void updateUserInfoToDB(User user) {

        DatabaseReference userDB = firebaseDatabase.getReference().child("users")
                .child(String.valueOf(sharedPreferences.getInt(SharedPref.USER_ID, 0)));

        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("name", user.getName());
        userInfoMap.put("phone", user.getPhone());
        userInfoMap.put("photo", user.getPhoto());
        userInfoMap.put("groupId", user.getGroupId());
        userInfoMap.put("userId", user.getUserId());

        userDB.updateChildren(userInfoMap);
    }


    private void getPermission(final User user) {

        Call<ResponseBody> call = retrofitInstance.getPermission(user.getUserId());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null) {
                    if (response.body().isCanViewGroupMembers() == true) {
                        editor.putBoolean(SharedPref.PERMISSION, true);
                        editor.apply();
                        groupId = user.getGroupId();
                        getAllGroupMemberLocation(user.getGroupId());
                    } else {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        editor.putBoolean(SharedPref.PERMISSION, false);
                        editor.apply();
                        Toast.makeText(getContext(), "Permission denied by captain to view other members", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void getAllGroupMemberLocation(int groupId) {
        groupMemberDB = firebaseDatabase.getReference().child("users").orderByChild("groupId").equalTo(groupId);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    memberLocationList.clear();
                    if (map != null) {
                        map.clear();
                    }

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child("name").exists() && data.child("latitude").exists()) {
                            MemberLocation memberLocation = data.getValue(MemberLocation.class);

                            if (memberLocation.getUserId() != sharedPreferences.getInt(SharedPref.USER_ID, 0)) {
                                setMarker(memberLocation);
                                memberLocationList.add(memberLocation);
                            }
                        }
                    }
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } else {
                    if (map != null) {
                        map.clear();
                    }
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        groupMemberDB.addValueEventListener(valueEventListener);


    }

    private void setMarker(MemberLocation memberLocation) {
        if (memberLocation.getPhoto() == null || memberLocation.getPhoto().equals("")) {
            if (getActivity() != null) {
                if (map!=null){
                    map.addMarker(new MarkerOptions().position(new LatLng(memberLocation.getLatitude(), memberLocation.getLongitude()))
                            .snippet(String.valueOf(memberLocation.getUserId())).icon(BitmapDescriptorFactory
                                    .fromBitmap(createCustomMarker("", memberLocation.getName()))));
                }

            }

        } else {
            if (getActivity() != null) {
                if (map!=null){
                    map.addMarker(new MarkerOptions().position(new LatLng(memberLocation.getLatitude(), memberLocation.getLongitude()))
                            .snippet(String.valueOf(memberLocation.getUserId())).icon(BitmapDescriptorFactory
                                    .fromBitmap(createCustomMarker(memberLocation.getPhoto(), memberLocation.getName()))));
                }

            }

        }

    }

    public Bitmap createCustomMarker(String photo, String name) {
        Bitmap bitmap = null;

        if (getActivity() != null) {
            View marker = ((LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

            ImageView markerImage = marker.findViewById(R.id.userProfileImageCIV);
            TextView nameTV = marker.findViewById(R.id.nameTV);
            if (photo != null && !photo.equals("")) {
                Glide.with(getActivity().getApplicationContext()).applyDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.my_profile_image)).load(RetrofitInstance.BASE_URL + photo).into(markerImage);

            }
            nameTV.setText(name);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
            marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            marker.buildDrawingCache();
            bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            marker.draw(canvas);

        }
        return bitmap;
    }

    private void getCaptainId(int groupId) {
        Call<ResponseBody> call = retrofitInstance.getCaptainId(groupId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null) {
                    getCaptainLocation(response.body().getCaptainId());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void getCaptainLocation(int captainId) {
        DatabaseReference captainDB = firebaseDatabase.getReference().child("users").child(String.valueOf(captainId));
        captainDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("name").exists() && dataSnapshot.child("latitude").exists()) {
                        MemberLocation captain = dataSnapshot.getValue(MemberLocation.class);
                        captainLatLng = new LatLng(captain.getLatitude(), captain.getLongitude());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void initializeMap() {
        googleMapOptions = new GoogleMapOptions();
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance(googleMapOptions);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.mapViewFrameLayout, supportMapFragment);
        fragmentTransaction.commitAllowingStateLoss();
        supportMapFragment.getMapAsync(this);
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

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(getActivity(), LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                getActivity().startForegroundService(serviceIntent);
            } else {
                getActivity().startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {

                return true;
            }
        }
        return false;
    }

    private void updateLocationToDB(Location location) {
        DatabaseReference userDB = firebaseDatabase.getReference().child("users")
                .child(String.valueOf(sharedPreferences.getInt(SharedPref.USER_ID, 0)));
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("latitude", location.getLatitude());
        locationMap.put("longitude", location.getLongitude());
        userDB.updateChildren(locationMap);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        getActivity().getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Type here..");
        View v = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        v.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
        ImageView searchClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchClose.setColorFilter(ContextCompat.getColor(getActivity(), R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);

        searchView.setOnQueryTextListener(this);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        query = query.toLowerCase();

        boolean isMatch = false;

        if (memberLocationList != null && memberLocationList.size() > 0) {
            for (MemberLocation memberLocation : memberLocationList) {
                if (memberLocation.getName().toLowerCase().contains(query)
                        || memberLocation.getPhone().toLowerCase().contains(query)) {
                    animateCamera(new LatLng(memberLocation.getLatitude(), memberLocation.getLongitude()), 21);
                    isMatch = true;
                    break;
                }
            }

            if (isMatch == false) {
                Toast.makeText(getActivity(), "No user found by " + query, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "No user found by " + query, Toast.LENGTH_SHORT).show();
        }


        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        changeMapStyle(map);

        if (locationPermissionsGranted) {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            getDeviceLocation();

            map.setMyLocationEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);


        }

        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

                if (visibleMyLocationBtn == true) {
                    //binding.myLocationFAB.show();
                }

            }
        });

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                visibleMyLocationBtn = true;
            }
        });


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                openBottomSheet(marker.getSnippet());
                return true;
            }
        });

    }


    private void openBottomSheet(String selectedUserID) {
        if (selectedUserID != null && !selectedUserID.equals("")) {
            Bundle bundle = new Bundle();
            bundle.putString("selectedUserID", selectedUserID);
            UserInfoBottomSheet userInfoBottomSheet = new UserInfoBottomSheet();
            userInfoBottomSheet.setArguments(bundle);
            userInfoBottomSheet.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "UserInfoBottomSheet");
        }
    }


    private void changeMapStyle(GoogleMap map) {
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    getActivity(), R.raw.map_style));
        } catch (Resources.NotFoundException e) {

        }
    }
}
