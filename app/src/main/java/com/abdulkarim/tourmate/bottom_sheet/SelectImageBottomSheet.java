package com.abdulkarim.tourmate.bottom_sheet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abdulkarim.tourmate.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class SelectImageBottomSheet extends BottomSheetDialogFragment {

    private SelectionListener mListener;
    private LinearLayout camera,gallery;

    public SelectImageBottomSheet(){

    }

    @SuppressLint("ValidFragment")
    public SelectImageBottomSheet(SelectionListener mListener) {
        this.mListener = mListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_select_image,container,false);
        camera = view.findViewById(R.id.cameraImageLayoutID);
        gallery = view.findViewById(R.id.galleryImageLayoutID);
        onClick();

        return view;
    }

    private void onClick() {
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCameraButtonClicked();

            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mListener.onGalleryButtonClicked();
            }
        });
    }
    public interface SelectionListener {
        void onCameraButtonClicked();
        void onGalleryButtonClicked();
    }
}
