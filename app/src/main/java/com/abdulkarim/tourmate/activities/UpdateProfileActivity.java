package com.abdulkarim.tourmate.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.bottom_sheet.SelectImageBottomSheet;
import com.abdulkarim.tourmate.databinding.ActivityUpdateProfileBinding;
import com.abdulkarim.tourmate.helper.EmailMatcher;
import com.abdulkarim.tourmate.helper.SharedPref;
import com.abdulkarim.tourmate.model.Group;
import com.abdulkarim.tourmate.model.ResponseBody;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileActivity extends AppCompatActivity implements SelectImageBottomSheet.SelectionListener {


    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_SELECT_PHOTO = 2;

    private ActivityUpdateProfileBinding binding;
    private SelectImageBottomSheet selectImageBottomSheet;
    private ApiService retrofitInstance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String currentPhotoPath;
    private String mobileNumber;
    private int groupId = 0;
    private String encodeImage = "";
    private boolean isForUpdate;
    private String photo;

    private List<Group> groupList;
    private List<String> groupNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_update_profile);

        if (getIntent().getExtras() != null) {
            mobileNumber = getIntent().getStringExtra("mobileNumber");
            isForUpdate = getIntent().getBooleanExtra("isForUpdate", false);

            if (isForUpdate == true) {
                binding.backBtnIV.setVisibility(View.VISIBLE);
                binding.groupTIL.setVisibility(View.GONE);
                binding.addressTIL.setVisibility(View.VISIBLE);
                binding.mobileNumberTIL.setVisibility(View.VISIBLE);
                binding.nameET.setText(getIntent().getStringExtra("name"));
                binding.emailET.setText(getIntent().getStringExtra("email"));
                groupId = getIntent().getIntExtra("groupId", 0);
                binding.addressET.setText(getIntent().getStringExtra("address"));
                binding.mobileNumberET.setText(mobileNumber);

                String gender = getIntent().getStringExtra("gender");
                if (gender.equals("Male")) {
                    binding.maleRBID.setChecked(true);
                } else if (gender.equals("Female")) {
                    binding.femaleRBID.setChecked(true);
                }

                photo = getIntent().getStringExtra("photo");

                if (photo != null && !photo.equals("")) {
                    Glide.with(getApplicationContext()).applyDefaultRequestOptions(new RequestOptions()
                            .placeholder(R.drawable.my_profile_image)).load(RetrofitInstance.BASE_URL + photo).into(binding.userProfileImageID);

                }

            } else {
                binding.emailTIL.setVisibility(View.GONE);
                binding.backBtnIV.setVisibility(View.GONE);
                binding.addressTIL.setVisibility(View.GONE);
                binding.mobileNumberTIL.setVisibility(View.GONE);
                binding.updateBtn.setText(R.string.title_register);
            }

        } else {
            mobileNumber = "01877516041";
            binding.emailTIL.setVisibility(View.GONE);
            binding.backBtnIV.setVisibility(View.GONE);
            binding.updateBtn.setText(R.string.title_register);
        }

        init();

        getGroupList();

        binding.backBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.imageCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectionBottomSheet();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameET.getText().toString();
                String gender = "";
                if (binding.maleRBID.isChecked()) {
                    gender = "Male";
                } else if (binding.femaleRBID.isChecked()) {
                    gender = "Female";
                }

                if (isForUpdate == true) {
                    String email = binding.emailET.getText().toString();
                    mobileNumber = binding.mobileNumberET.getText().toString();
                    String address = binding.addressET.getText().toString();
                    if (name.equals("") || name.isEmpty()) {
                        binding.nameET.setError("Enter your name");
                        binding.nameET.requestFocus();
                    } else if (!email.equals("") && EmailMatcher.validate(email) == false) {
                        binding.emailET.setError("Enter a valid email");
                        binding.emailET.requestFocus();
                    }else {
                        if (mobileNumber.matches("01[0-9]{9}")){
                            binding.updateBtn.setVisibility(View.GONE);
                            binding.progressBar.setVisibility(View.VISIBLE);
                            updateDate(name, gender, email, address);
                        }else {
                            binding.mobileNumberET.setError("Enter a valid mobile number");
                            binding.mobileNumberET.requestFocus();
                        }

                    }
                } else {
                    if (groupId == 0) {
                        binding.groupACT.setError("Select group name");
                        binding.groupACT.requestFocus();
                    } else if (name.equals("") || name.isEmpty()) {
                        binding.nameET.setError("Enter your name");
                        binding.nameET.requestFocus();
                    } else {
                        binding.updateBtn.setVisibility(View.GONE);
                        binding.progressBar.setVisibility(View.VISIBLE);
                        saveData(groupId, name, gender, mobileNumber);
                    }
                }

            }
        });

        binding.groupACT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String groupName = binding.groupACT.getText().toString();

                for (Group group : groupList) {
                    if (group.getName().equals(groupName)) {
                        groupId = group.getId();
                        break;
                    }
                }
            }
        });

    }

    private void init() {
        sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        groupList = new ArrayList<>();
        retrofitInstance = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
    }

    private void getGroupList() {

        Call<List<Group>> call = retrofitInstance.getGroups();

        call.enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response != null) {
                    groupList = response.body();

                    groupNameList = new ArrayList<>();

                    if (groupList != null && groupList.size() > 0) {
                        for (Group group : groupList) {
                            groupNameList.add(group.getName());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>
                                (UpdateProfileActivity.this, R.layout.custom_auto_complete_textview_layout, R.id.itemTV, groupNameList);
                        binding.groupACT.setThreshold(1);
                        binding.groupACT.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {

            }
        });
    }

    private void openSelectionBottomSheet() {
        selectImageBottomSheet = new SelectImageBottomSheet(this);
        selectImageBottomSheet.show(getSupportFragmentManager(), "selectImage");
    }

    private void updateDate(String name, String gender, String email, String address) {

        Call<ResponseBody> call = retrofitInstance.updateProfile(sharedPreferences.getInt(SharedPref.USER_ID, 0), name, email, gender, encodeImage, address, mobileNumber);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response != null) {
                    if (response.body().isStatus() == true) {
                        onBackPressed();
                    } else {
                        Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.updateBtn.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void saveData(int groupId, String name, String gender, String mobileNumber) {

        Call<ResponseBody> call = retrofitInstance.registerUser(mobileNumber, name, gender, groupId, encodeImage);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null) {

                    Log.d("Karim", "onResponse: " + response.body());

                    if (response.body().isStatus() == true) {
                        editor.putInt(SharedPref.USER_ID, response.body().getMemberId());
                        editor.apply();


                        startActivity(new Intent(UpdateProfileActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));

                    } else {
                        binding.updateBtn.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(UpdateProfileActivity.this, "Something went wrong please contact with admin", Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public void onCameraButtonClicked() {

        dispatchTakePictureIntent();
        selectImageBottomSheet.dismiss();
    }

    @Override
    public void onGalleryButtonClicked() {

        Intent intent_gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent_gallery, REQUEST_SELECT_PHOTO);
        selectImageBottomSheet.dismiss();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.abdulkarim.tourmate.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setImageToView();

        } else if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK) {


            Uri uri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                setImageToImageView(bitmap);
            }
        }
    }

    private void setImageToView() {
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        setImageToImageView(bitmap);
    }

    private void setImageToImageView(Bitmap bitmap) {
        encodeImage = encodeImageToBase64String(bitmap);
        Log.d("Karim", encodeImage);
        binding.userProfileImageID.setImageBitmap(bitmap);
    }

    private String encodeImageToBase64String(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return imageString;
    }

    private Bitmap decodeImageToBitmap(String imageString) {
        byte[] imageBytes2 = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes2, 0, imageBytes2.length);
        return decodedImage;
    }
}
