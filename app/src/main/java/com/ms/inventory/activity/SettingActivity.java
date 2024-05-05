package com.ms.inventory.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ms.inventory.utils.PreferenceManager;
import com.ms.inventory.R;

import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";

    EditText edtHost, edtOutletCode;
    Spinner spnZoneName;

    List<String> zoneList = Arrays.asList("Front Office", "Back Office", "Other");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Global Configure");
        }

        edtHost = findViewById(R.id.edt_host);
        spnZoneName = findViewById(R.id.spn_zone_name);
        edtOutletCode = findViewById(R.id.edt_outlet_code);

        PreferenceManager pref = new PreferenceManager(this);

        spnZoneNameSetup();

        edtHost.setText(pref.getHost());
        edtHost.setSelection(edtHost.getText().length());

        String zone = pref.getZoneName();
        int position = 0;
        if (zone.equalsIgnoreCase("FrontOffice")) {
            position = 0;
        } else if (zone.equalsIgnoreCase("BackOffice")) {
            position = 1;
        } else {
            position = 2;
        }

        Log.e(TAG, "onCreate: position: " + position);
        spnZoneName.setSelection(position);

        edtOutletCode.setText(pref.getOutletCode());

    }

    private void spnZoneNameSetup() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this , android.R.layout.simple_spinner_item, zoneList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnZoneName.setAdapter(adapter);
    }


    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancel) {
            finish();
        } else if (v.getId() == R.id.btn_save) {
            savePreference();
        }
    }


    private void savePreference() {
        String ip = edtHost.getText().toString().trim();
        String deviceId = MainPreferencesActivity.MainPreferenceFragment.DeviceUtils.getDeviceId(this);
        String zoneName = spnZoneName.getSelectedItem().toString().replace(" ", "");
        Log.e(TAG, "savePreference: zone name: " + zoneName.trim());
        String outletCode = edtOutletCode.getText().toString().trim();


        if (!zoneName.isEmpty() && !outletCode.isEmpty()) {
            new PreferenceManager(this).saveData(ip, deviceId, zoneName, outletCode);
            Toast.makeText(SettingActivity.this, "Setting save successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {

            if (outletCode.isEmpty()) {
                edtOutletCode.setError("Enter Outlet Code");
            }
        }


    }



}
