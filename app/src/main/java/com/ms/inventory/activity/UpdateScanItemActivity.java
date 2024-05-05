package com.ms.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ms.inventory.R;
import com.ms.inventory.utils.DBHelper;
import com.ms.inventory.utils.Utils;

import mehdi.sakout.fancybuttons.FancyButton;

public class UpdateScanItemActivity extends AppCompatActivity {

    private static final String TAG = "UpdateScanItemActivity";

    private DBHelper DB;
    private EditText edtuScanQty;
    String barcode, itemDescription, scanQty;
    Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_scan_item);

        TextView tvuBarcode = findViewById(R.id.tvu_barcode_id);
        TextView tvuDescription = findViewById(R.id.tvu_description_id);
        edtuScanQty = findViewById(R.id.edtu_scan_qty_id);
        FancyButton btnuCancel = findViewById(R.id.btnu_cancel_id);
        FancyButton btnuSave = findViewById(R.id.btnu_save_id);
        FancyButton btnuDelete = findViewById(R.id.btnu_delete_id);

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

    //    private void processForUpdate() {
//
//        String newQty = edtuScanQty.getText().toString().trim();
//        Double newDoubleQty = Double.parseDouble(newQty);
//
//        if (newQty.isEmpty()) {
//            Utils.errorDialog(this, "Empty Quantity!", "Scan Quantity is empty. Please enter quantity");
//            errorVibration();
//            return;
//        }
//
//        try {
//            if (newDoubleQty <= 0) {
//                Utils.errorDialog(this, "Zero Quantity!", "Zero Quantity is not allowed. Please enter positive quantity");
//                errorVibration();
//                return;
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "processForUpdate: " + e);
//        }
//
//        if (newDoubleQty.equals(Double.parseDouble(scanQty))) {
//            finish();
//        } else {
//            DB.updateScanItem(barcode, newQty);
//            Toast.makeText(UpdateScanItemActivity.this, "Item Updated", Toast.LENGTH_SHORT).show();
////            ViewScanItems.getInstance().loadScanItems();
//            ViewScanItems.getInstance().refreshRecyclerView();
//            finish();
//        }
//    }

    private void processForUpdate() {
        String newQty = edtuScanQty.getText().toString().trim();

        if (newQty.isEmpty()) {
            Utils.errorDialog(this, "Empty Quantity!", "Scan Quantity is empty. Please enter quantity");
            errorVibration();
            return;
        }

        double newDoubleQty = Double.parseDouble(newQty);
        String newDoubleQtyStr = String.valueOf(newDoubleQty);

        try {
            if (newDoubleQty <= 0) {
                Utils.errorDialog(this, "Zero Quantity!", "Zero Quantity is not allowed. Please enter positive quantity");
                errorVibration();
                return;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "processForUpdate: " + e);
            return;
        }

        if (newDoubleQtyStr.equals(scanQty)) {
            finish();
        } else {
            DB.updateScanItem(barcode, newDoubleQtyStr);
            ViewScanItems.getInstance().updateScanItem(barcode, newDoubleQtyStr); // Update the item in the list
            Toast.makeText(UpdateScanItemActivity.this, "Item Updated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void deleteConfirmationDialog() {
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