package com.ms.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ms.inventory.R;
import com.ms.inventory.utils.DBHelper;
import com.ms.inventory.utils.Utils;

import mehdi.sakout.fancybuttons.FancyButton;

public class UpdateScanItemActivity extends AppCompatActivity {

    private static final String TAG = "UpdateScanItemActivity";

    private TextView tvuBarcode, tvuDescription;
    private EditText edtuScanQty;
    private FancyButton btnuCancel, btnuSave, btnuDelete;
    private DBHelper DB;
    String barcode, itemDescription, scanQty;
    Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_scan_item);

        tvuBarcode = findViewById(R.id.tvu_barcode_id);
        tvuDescription = findViewById(R.id.tvu_description_id);
        edtuScanQty = findViewById(R.id.edtu_scan_qty_id);
        btnuCancel = findViewById(R.id.btnu_cancel_id);
        btnuSave = findViewById(R.id.btnu_save_id);
        btnuDelete = findViewById(R.id.btnu_delete_id);

        DB = new DBHelper(UpdateScanItemActivity.this);

        barcode = getIntent().getStringExtra("barcode");
        itemDescription = getIntent().getStringExtra("itemDescription");
        scanQty = getIntent().getStringExtra("scanQty");

        tvuBarcode.setText(barcode);
        tvuDescription.setText(itemDescription);
        edtuScanQty.setText(scanQty);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        btnuCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnuSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processForUpdate();
            }
        });

        btnuDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorVibration();
                deleteConfirmationDialog();
            }
        });
    }

    private void processForUpdate() {

        String scanQty = edtuScanQty.getText().toString().trim();

        if (scanQty.isEmpty()) {
            Utils.errorDialog(this, "Empty Quantity!", "Scan Quantity is empty. Please enter quantity");
            errorVibration();
            return;
        }

        try {
            if (Integer.parseInt(scanQty)<=0) {
                Utils.errorDialog(this, "Zero Quantity!", "Zero Quantity is not allowed. Please enter positive quantity");
                errorVibration();
                return;
            }
        }catch (Exception e){

        }

        DB.updateScanItem(barcode, edtuScanQty.getText().toString());
        Toast.makeText(UpdateScanItemActivity.this, "Item Updated", Toast.LENGTH_SHORT).show();
        ViewScanItems.getInstance().loadScanItems();
        finish();
    }

    private void deleteConfirmationDialog(){
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Confirmation");
        d.setMessage("Are you want to delete item");
        d.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        d.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                processForDelete();

            }
        });
        d.create().show();
    }

    private void processForDelete() {
        boolean checkDeleteData = DB.deleteSingleData(barcode);
        if (checkDeleteData) {
            Toast.makeText(UpdateScanItemActivity.this, "Item delete successfully!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onResponse: Item delete successfully!");
            ViewScanItems.getInstance().loadScanItems();
            finish();
            //Toast.makeText(ScanActivity.this, "Data delete successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(UpdateScanItemActivity.this, "Item delete failed!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onResponse: Item delete failed!");
            //Toast.makeText(ScanActivity.this, "Data delete failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void errorVibration() {

        mVibrator.vibrate(600);

    }
}