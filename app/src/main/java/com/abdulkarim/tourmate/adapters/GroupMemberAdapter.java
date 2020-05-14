package com.abdulkarim.tourmate.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.ModelGroupMemberItemBinding;
import com.abdulkarim.tourmate.fragments.ImageZoomingDialog;
import com.abdulkarim.tourmate.helper.SharedPref;
import com.abdulkarim.tourmate.helper.StaticKeys;
import com.abdulkarim.tourmate.model.GroupMember;
import com.abdulkarim.tourmate.model.ResponseBody;
import com.abdulkarim.tourmate.retrofit.ApiService;
import com.abdulkarim.tourmate.retrofit.RetrofitInstance;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
    private List<GroupMember> memberList;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ApiService retrofitInstance;
    private OnPermissionSave onPermissionSave;
    private Activity activity;

    public GroupMemberAdapter(List<GroupMember> memberList, Context context, OnPermissionSave onPermissionSave) {
        this.memberList = memberList;
        this.context = context;
        this.onPermissionSave = onPermissionSave;
        sharedPreferences = context.getSharedPreferences(SharedPref.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        retrofitInstance = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ModelGroupMemberItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext())
                , R.layout.model_group_member_item, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final GroupMember member = memberList.get(i);

        if (member.getMemberType().equals(StaticKeys.CAPTAIN)){
            viewHolder.binding.captainIV.setVisibility(View.VISIBLE);
        }else {
            viewHolder.binding.captainIV.setVisibility(View.GONE);
        }
        if (member.getPhoto() != null && !member.getPhoto().equals("")) {
            Glide.with(context.getApplicationContext()).applyDefaultRequestOptions(new RequestOptions()
                    .placeholder(R.drawable.my_profile_image)).load(RetrofitInstance.BASE_URL + member.getPhoto()).into(viewHolder.binding.profileImageCIV);

        }else {
            viewHolder.binding.profileImageCIV.setImageDrawable(context.getResources().getDrawable(R.drawable.my_profile_image));
        }

        viewHolder.binding.profileImageCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (member.getPhoto()!=null && !member.getPhoto().equals("")){
                    openImageZoomingDialog(RetrofitInstance.BASE_URL + member.getPhoto());
                }
            }
        });

        viewHolder.binding.nameTV.setText(member.getName());
        viewHolder.binding.mobileNumberTV.setText(member.getPhone());
        viewHolder.binding.callBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + member.getPhone()));
                context.startActivity(intent);
            }
        });



        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getString(SharedPref.MEMBER_TYPE,"").equals(StaticKeys.CAPTAIN)
                        && member.getMemberType().equals(StaticKeys.MEMBER)){
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.dialog_permission);

                    Window window = dialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView cancelTV = dialog.findViewById(R.id.cancelTV);
                    TextView doneTV = dialog.findViewById(R.id.doneTV);
                    final CheckBox canViewMembersCB = dialog.findViewById(R.id.canViewMembersCB);
                    if (member.isCanViewGroupMembers() == true) {
                        canViewMembersCB.setChecked(true);
                    } else {
                        canViewMembersCB.setChecked(false);
                    }

                    cancelTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    doneTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String canViewMembers = "";

                            if (canViewMembersCB.isChecked()==true){
                                canViewMembers = "on";
                            }else if (canViewMembersCB.isChecked()==false){
                                canViewMembers = "off";
                            }

                            savePermission(member,canViewMembers);
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });


    }

    private void savePermission(GroupMember member,String canViewMembers) {

        retrofitInstance.savePermission(member.getUserId(),canViewMembers,2).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response!=null){
                    if (response.body().isStatus()==true){
                        onPermissionSave.onSuccess(response.body().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ModelGroupMemberItemBinding binding;

        public ViewHolder(ModelGroupMemberItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnPermissionSave{
        void onSuccess(String message);
    }


    private void openImageZoomingDialog(String imageUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", imageUrl);
        ImageZoomingDialog dialog = new ImageZoomingDialog();
        dialog.setArguments(bundle);
        FragmentTransaction ft = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
        dialog.show(ft, "TAG");
    }

}
