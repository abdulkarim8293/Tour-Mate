package com.abdulkarim.tourmate.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.abdulkarim.tourmate.R;


public class GpsProvider {

    private Context context;
    private Activity activity;

    public GpsProvider(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }


    public void showDialogForLocation() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View mView = activity.getLayoutInflater().inflate(R.layout.dialog_location, null);

        Button settings = mView.findViewById(R.id.settingsBtnID);

        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dialog.dismiss();
            }
        });
    }
    public boolean statusCheck() {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;

        }

        return true;
    }
}
