package com.ms.inventory.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ms.inventory.R;
import com.ms.inventory.utils.AppController;
import com.ms.inventory.utils.DBHelper;
import com.ms.inventory.utils.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionActivity extends AppCompatActivity {

    DBHelper DB;
    private PreferenceManager pref;
    ProgressDialog progressDialog;
    List<String> sessionIdList = new ArrayList<>();

    // Get the current date
    Date currentTime = Calendar.getInstance().getTime();
    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentTime);
    private long pressedTime;

    TextView quantityTextView;
    private LinearLayout buttonLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        DB = new DBHelper(this);
        pref = new PreferenceManager(this);
        quantityTextView = findViewById(R.id.tv_qty);
        buttonLayout = findViewById(R.id.button_layout);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting New Session...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        getSessionId();


    }

    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            logout();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

    //    private void showLogoutConfirmationDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Logout Confirmation");
//        builder.setMessage("Are you sure you want to log out?");
//        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                logout();
//            }
//        });
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
    private void logout() {
        Intent intent = new Intent(SessionActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void getSessionId() {
        String url = pref.getBaseUrl() + "/ValuesApi/GetSession?FromDate=2024-01-01&ToDate=" + currentDate;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String sessionId = jsonObject.getString("SessionId");
                                sessionIdList.add(sessionId);
                            }

                            // Update TextView
                            quantityTextView.setText(getString(R.string.select_a_session_id));

                            // Add buttons dynamically based on the quantity
                            for (String sessionId : sessionIdList) {
                                boolean isSessionUSed = DB.isSessionUsed(sessionId);
                                if (!isSessionUSed) {
                                    addButton(sessionId);
                                }
                            }

                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e("getSessionId Error", "GetSessionId Error occurred", error);
                    }
                }

        );

        // Add the request to the RequestQueue.
        AppController.getInstance().addToRequestQueue(stringRequest);


    }

    // Method to add button dynamically
    private void addButton(final String sessionId) {
        Button button = new Button(this);
        button.setText(String.format("%s - %s", getString(R.string.session), sessionId));
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the clicked session ID in PreferenceManager
                pref.setSession(sessionId);

                finish();
                Intent intent = new Intent(SessionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        buttonLayout.addView(button);
    }

}

