package com.ms.inventory.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ms.inventory.R;
import com.ms.inventory.model.InventoryData;
import com.ms.inventory.model.MainItemList;
import com.ms.inventory.utils.AppController;
import com.ms.inventory.utils.DBHelper;
import com.ms.inventory.utils.PreferenceManager;
import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends AppCompatActivity implements DBHelper.DataInsertionCallback {

    ProgressDialog progressDialog;
    private static final String TAG = "MainActivity";

    PreferenceManager pref;
    DBHelper DB;
    String sessionId;
    int totalDatabaseItem;

    TextView tvItemQty, tvSession;
    FancyButton btnImport, btnAdjust;

    private long pressedTime;

    @Override
    public void onDataInsertionCompleted() {
        progressDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DB = new DBHelper(this);
        pref = new PreferenceManager(this);
        sessionId = pref.getSession();

        btnImport = findViewById(R.id.btn_import);
        btnAdjust = findViewById(R.id.btn_adjust);
        tvItemQty = findViewById(R.id.tv_qty);
        tvSession = findViewById(R.id.tv_session);

        if (pref.isOnlineMode()) {
            loadInitialData();

            btnImport.setVisibility(View.GONE);
            tvItemQty.setVisibility(View.GONE);
        } else {
            btnImport.setVisibility(View.VISIBLE);
            tvItemQty.setVisibility(View.VISIBLE);
        }

        try {
            totalDatabaseItem = DB.countTableItem();
        } catch (Exception e) {
            Log.e("Abir001", "onCreate: ", e);
        }


        if (pref.getSession().isEmpty()) {
            tvSession.setText(R.string.no_session_selected);
        } else {
            tvSession.setText(String.format("Session - %s", pref.getSession()));
        }

//		Log.e(TAG, "onCreate: mac: " + getMacAddress());
        if (!TextUtils.isEmpty(getMacAddress())) {
            pref.setMacAddress(getMacAddress());
        }
    }

    public void loadInitialData() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Offline Inventory Data...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        DB = new DBHelper(MainActivity.this);

        String baseUrl = pref.getBaseUrl();
        String url = baseUrl + "ValuesApi/GetSessionList?SessionId=" + sessionId + "&PageNo=1&DataRowSize=1";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse JSON response
                            JSONArray responseArray = new JSONArray(response);
                            JSONObject responseObject = responseArray.getJSONObject(0);
                            int totalPage = responseObject.getInt("TotalPage");

                            // Checks if database has already same amount of data from online inventory
                            if (totalPage != totalDatabaseItem) {
                                saveFullInventory(totalPage);
                            } else {
                                progressDialog.dismiss();
                            }
//								for (int i = 1; i <= totalPage+1; i++) {
//									saveFullInventory(i);
//								}

                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Log.e("inventoryData", "Error parsing JSON response: " + e.getMessage());
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.e(TAG, "Failed to fetch inventory data: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public void saveFullInventory(final int page) {

        String baseUrl = pref.getBaseUrl();
        DB = new DBHelper(MainActivity.this);
//		String url = baseUrl + "ValuesApi/GetSessionList?SessionId=1000120240318153358&PageNo="+Integer.toString(page)+"&DataRowSize=50";
        String url = baseUrl + "ValuesApi/GetSessionList?SessionId=" + sessionId + "&PageNo=1&DataRowSize=" + Integer.toString(page);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse JSON response
                            JSONArray responseArray = new JSONArray(response);
                            JSONObject responseObject = responseArray.getJSONObject(0);
                            boolean status = responseObject.getBoolean("Status");

                            if (status) {
                                List<InventoryData.Data> inventoryList = new ArrayList<>();
                                JSONArray dataArray = responseObject.getJSONArray("Data");

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject dataObject = dataArray.getJSONObject(i);

                                    InventoryData.Data data = new InventoryData.Data();
                                    data.setSessionId(dataObject.getString("SessionId"));
                                    data.setBarcode(dataObject.getString("Barcode"));
                                    data.setsBarcode(dataObject.getString("sBarcode"));
                                    data.setUserBarcode(dataObject.getString("USER_BARCODE"));
                                    data.setStartQty(dataObject.getDouble("StartQty"));
                                    data.setScanQty(dataObject.getDouble("ScanQty"));
                                    data.setScanStartDate(dataObject.getString("ScanStartDate"));
                                    data.setMrp(dataObject.getDouble("MRP"));
                                    data.setDescription(dataObject.getString("Description"));
                                    data.setCpu(dataObject.getDouble("CPU"));

                                    inventoryList.add(data);
                                }
                                // Save data to database
                                DB.addInventoryData(inventoryList, MainActivity.this);

                                Log.d("inventoryData", "Inventory data saved successfully.");
                            } else {
                                progressDialog.dismiss();
                                // Handle case where status is false
                                String message = responseObject.getString("Message");
                                Log.e("inventoryData", "Failed to fetch inventory data: " + message);
                            }

                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Log.e("inventoryData", "Error parsing JSON response: " + e.getMessage());
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.e("inventoryData", "Failed to fetch inventory data: " + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Mac Address")
                            .setCancelable(false)
                            .setMessage("No mac address found!")
                            .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    getMacAddress();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }

                return res1.toString();
            }
        } catch (Exception ignored) {
        }

        return null;
//		return "02:00:00:00:00:00";
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateItems();    //counting items
    }

    private void updateItems() {
//		long qty = SugarRecord.count(MainItemList.class);
        long qty = totalDatabaseItem;

        if (qty == 0) {
            qty = SugarRecord.count(MainItemList.class);
        }

        if (qty > 0) {
            tvItemQty.setText("Total Items:  " + qty);
        } else {
            tvItemQty.setText(R.string.no_items_are_found);
        }
    }

    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.btn_item) {
            startActivity(new Intent(this, ScanActivity.class));
        } else if (id == R.id.btn_adjust) {
            startActivity(new Intent(this, AdjustActivity.class));
        } else if (id == R.id.btn_import) {
            startActivity(new Intent(this, ImportActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            logout();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

    private void logout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
