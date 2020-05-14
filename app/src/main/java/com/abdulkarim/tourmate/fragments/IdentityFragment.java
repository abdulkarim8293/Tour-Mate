package com.abdulkarim.tourmate.fragments;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abdulkarim.tourmate.R;
import com.abdulkarim.tourmate.bottom_sheet.SelectImageBottomSheet;
import com.abdulkarim.tourmate.databinding.FragmentIdentityBinding;

public class IdentityFragment extends Fragment implements SelectImageBottomSheet.SelectionListener {

    private FragmentIdentityBinding binding;
    private SelectImageBottomSheet selectImageBottomSheet;

    public IdentityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_identity, container, false);

        binding.addNationalId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectionBottomSheet();
            }
        });

        binding.addPassport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectionBottomSheet();
            }
        });

        return binding.getRoot();
    }

    private void openSelectionBottomSheet() {
        selectImageBottomSheet = new SelectImageBottomSheet(this);
        selectImageBottomSheet.show(getActivity().getSupportFragmentManager(), "selectImage");
    }

    @Override
    public void onCameraButtonClicked() {

    }

    @Override
    public void onGalleryButtonClicked() {

    }
}
