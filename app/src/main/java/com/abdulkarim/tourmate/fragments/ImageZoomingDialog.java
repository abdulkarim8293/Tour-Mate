package com.abdulkarim.tourmate.fragments;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.databinding.DialogImageZoomingBinding;
import com.bumptech.glide.Glide;

public class ImageZoomingDialog extends DialogFragment {

    private DialogImageZoomingBinding binding;
    private Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater,R.layout.dialog_image_zooming, container, false);

        Bundle bundle = getArguments();
        String imageUrl = bundle.getString("imageUrl", "");

        Glide.with(getActivity().getApplicationContext()).load(imageUrl).into(binding.photoView);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        if (dialog != null) {
            //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getActivity().getResources().getColor(R.color.transparent_black)));
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
