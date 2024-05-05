package com.ms.inventory.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ms.inventory.R;
import com.ms.inventory.adapter.ItemSearchAdapter;
import com.ms.inventory.model.ItemInventory;
import com.ms.inventory.model.ItemSearch;
import com.ms.inventory.model.MainItemList;
import com.ms.inventory.utils.AppController;
import com.ms.inventory.utils.JsonValidator;
import com.ms.inventory.utils.PreferenceManager;
import com.ms.inventory.utils.Utils;
import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AdjustActivity extends AppCompatActivity {

    private static final String TAG = "AdjustActivity";

    public static final int REQUEST_CODE_ITEM_SEARCH = 101;
    PreferenceManager pref;

    EditText edtBarcode, edtItemCode, edtStockQty, edtScanQty, edtAdjustedQty, edtAdjustQty;
    private String userId;
    private String deviceId;
    private String zoneName;
    private String outletCode;
    private double adjQty;
    private double scanQty;

    private ItemSearch item;

    String searchType = "";

    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_SCAN = "barcode";

    Vibrator mVibrator;

    private boolean SAVE_WITH_ENTER_KEY = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        pref = new PreferenceManager(this);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        item = new ItemSearch();

        edtBarcode = (EditText) findViewById(R.id.edt_barcode);
        edtItemCode = (EditText) findViewById(R.id.edt_item_code);
        edtStockQty = (EditText) findViewById(R.id.edt_stock_qty);
        edtScanQty = (EditText) findViewById(R.id.edt_scan_qty);
        edtAdjustedQty = (EditText) findViewById(R.id.edt_adjusted_qty);
        edtAdjustQty = (EditText) findViewById(R.id.edt_adjust_qty);

        clearLayout();

        this.userId = pref.getUser();
        this.deviceId = pref.getDeviceId();
        this.zoneName = pref.getZoneName();
        String currentDate = Utils.getTodayDate();
        this.outletCode = pref.getOutletCode();

        edtBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                itemSearch();
                return true;
            } else if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                itemSearch();

                return true;
            }
            return false;
        });

        edtAdjustQty.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                Log.e("TAG", "edtScanQty KEYCODE_ENTER");
                SAVE_WITH_ENTER_KEY = true;
                processForSaveItem();

                return true;

            }

            return false;
        });
    }

    private void clearLayout() {
        edtBarcode.setText("");
        edtItemCode.setText("");
        edtStockQty.setText("");
        edtScanQty.setText("");
        edtAdjustedQty.setText("");
        edtAdjustQty.setText("");

        edtBarcode.requestFocus();

        // request barcode edittext if using keyboard enter key
        if (SAVE_WITH_ENTER_KEY) {
            focusBarcodeEditText();
            SAVE_WITH_ENTER_KEY = false;
        }
    }

    Timer barcodeFocusTimer;
    TimerTask barcodeFocusTimerTask;
    private void focusBarcodeEditText() {

        // cancel timer if already created
        if (barcodeFocusTimerTask != null) {
            barcodeFocusTimerTask.cancel();
            barcodeFocusTimerTask = null;

            barcodeFocusTimer.cancel();
            barcodeFocusTimer.purge();
        }

        // create new timer
        barcodeFocusTimer = new Timer();
        barcodeFocusTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> edtBarcode.requestFocus());
            }
        };
        barcodeFocusTimer.schedule(barcodeFocusTimerTask, 1000);

    }

    private void itemSearch() {
        String barcode = edtBarcode.getText().toString().trim();
        if (!barcode.isEmpty()) {
            barcodeProcess(barcode);
        }
    }

    private void barcodeProcess(String barcode) {
        //Log.e("TAG","Barcode: "+ barcode);

        if (pref.isOnlineMode()) {
            barcodeProcessForOnline(barcode.trim());
        } else {
            barcodeProcessForOffline(barcode.trim());
        }
    }

    private void barcodeProcessForOffline(String barcode) {
        //Log.e("TAG", "code: "+ barcode);
        List<MainItemList> listItems = SugarRecord.find(MainItemList.class, "barcode=?", barcode);

        //Log.e("TAG", "listItems: " + listItems.size());
        final List<ItemSearch> list = new ArrayList<>();

        for (MainItemList item : listItems) {
            ItemSearch is = new ItemSearch(item.itemCode, barcode, item.itemDescription, item.stockQty, item.saleQty, item.salePrice);
            list.add(is);
        }

        if (list.size() > 1) {
            //select Item From List
            showBarcodeMultipleItemDialog(list);
            //searchType="barcode";
        } else if (list.size() == 1) {
            item = list.get(0);
            searchType = TYPE_SCAN;
            updateLayout(item);

        } else {
            // search Item if barcode process failed
            barcodeProcessFailed();
        }
    }

    private void barcodeProcessForOnline(final String barcode) {

        final List<ItemSearch> list = new ArrayList<>();

        //barcode = "123";
        String baseUrl = pref.getBaseUrl();
        String depoCode = pref.getOutletCode();
        String url = baseUrl + "Data/GetByBarcode?barcode=" + barcode + "&depoCode=" + depoCode + "&searchText=";
        Log.e("TAG", "GetByBarcode URL: " + url);

        StringRequest strq = new StringRequest(Request.Method.GET, url, response -> {
            Log.e("TAG", response);
            JsonValidator jv = new JsonValidator();

            try {
                JSONObject json = new JSONObject(response);
                boolean status = jv.getBoolean(json, "Status");

                if (status) {
                    JSONArray data = json.getJSONArray("ReturnData");

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject j = data.getJSONObject(i);

                        String itemCode = jv.getString(j, "PRODUCTCODE");
                        String name = jv.getString(j, "PRODUCTNAME");
                        double currentStock = jv.getDouble(j, "CurrentStock");
                        double sale = jv.getDouble(j, "SALES");
                        double unitPrice = jv.getDouble(j, "UnitPrice");

                        ItemSearch itemSearch = new ItemSearch(itemCode, barcode, name, (int) currentStock, (int) sale, unitPrice);
                        list.add(itemSearch);
                        //Log.e("TAG", code + "  " + name + "  " + sale);
                    }
                }


                //Log.e("TAG", list.size()+"  ");
                if (list.size() > 1) {
                    //select Item From List
                    showBarcodeMultipleItemDialog(list);
                    //searchType="barcode";
                } else if (list.size() == 1) {
                    item = list.get(0);
                    searchType = TYPE_SCAN;
                    updateLayout(item);

                } else {
                    // search Item if barcode process failed
                    barcodeProcessFailed();
                }


            } catch (JSONException e) {
                Log.e(TAG, "barcodeProcessForOnline: ",e );
            }

        }, error -> {
            //Log.e("TAG", "Error ", error);
            errorVibration();

            Utils.errorDialog(AdjustActivity.this, "Connection Error", error.getMessage());
            edtBarcode.requestFocus();
        });

        AppController.getInstance().addToRequestQueue(strq);
    }

    private void showBarcodeMultipleItemDialog(final List<ItemSearch> list) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Select a Product");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 50, 20, 50);

        ListView listView = new ListView(this);
        layout.addView(listView);

        ItemSearchAdapter adapter = new ItemSearchAdapter(AdjustActivity.this, list);
        listView.setAdapter(adapter);

        d.setView(layout);

        final AlertDialog dialog = d.create();
        dialog.show();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            item = list.get(position);
            searchType = TYPE_SCAN;
            updateLayout(item);
            dialog.dismiss();
        });

    }

    private void barcodeProcessFailed() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Error!");
        d.setMessage("No Item Found. Would you like to search item?");

        d.setNegativeButton("NO", (dialog, which) -> {
            dialog.dismiss();
            clearLayout();
        });
        d.setPositiveButton("YES", (dialog, which) -> startActivityForResult(new Intent(AdjustActivity.this, SearchActivity.class), REQUEST_CODE_ITEM_SEARCH));

        d.create().show();
        errorVibration();
    }

    private void updateLayout(ItemSearch item) {
        if (item != null) {
            edtItemCode.setText(item.itemCode);
            Log.e(TAG, "updateLayout: " + item.barcode);
            edtBarcode.setText(item.barcode + "");
            edtStockQty.setText(item.currentStock + "");
            getScanItemInfo(item);
            edtAdjustQty.requestFocus();
        }


        edtScanQty.setText("");
        // set cursor at last position
        edtBarcode.setSelection(edtBarcode.getText().length());
        edtScanQty.setSelection(edtScanQty.getText().length());

    }

    private void totalScanQtyShow(ItemSearch item) {

        //String itemCode = edtItemCode.getText().toString().trim();

        if (!pref.isOnlineMode()) {

            List<ItemInventory> cList = SugarRecord.find(ItemInventory.class, "item_Code=?", item.itemCode);
            int count = 0;
            for (ItemInventory itemInventory : cList) {
                count += (int) itemInventory.scanQty;
            }

            edtScanQty.setText(count + "");

        } else {
            String baseUrl = pref.getBaseUrl();

            String deviceId = this.deviceId;
            String userId = this.userId;
            String zoneName = this.zoneName;
            String depoCode = this.outletCode;
            //String url = "http://192.168.1.24/agora/api/Data/GetScanItemInfo?barcodeItemcode=333&deviceId=3&userId=3&zoneName=w&depoCode=3";
			/*String url = baseUrl + "Data/GetItemScanQty?barcodeItemcode=" + item.itemCode + "&deviceId=" + deviceId + "&userId=" + userId +
				  "&zoneName=" + zoneName + "&depoCode=" + depoCode;*/

            String url = baseUrl + "Data/GetItemScanQty";
            try {
//				url += "?barcodeItemcode=" + URLEncoder.encode(item.itemCode, getString(R.string.utf_8)) +
//				url += "?barcodeItemcode=" + URLEncoder.encode(pref.getBarCode(), getString(R.string.utf_8)) +
                url += "?barcodeItemcode=" + URLEncoder.encode(item.barcode, getString(R.string.utf_8)) +
                        "&deviceId=" + URLEncoder.encode(deviceId, getString(R.string.utf_8)) +
                        "&userId=" + URLEncoder.encode(userId, getString(R.string.utf_8)) +
                        "&zoneName=" + URLEncoder.encode(zoneName, getString(R.string.utf_8)) +
                        "&depoCode=" + URLEncoder.encode(depoCode, getString(R.string.utf_8));

            } catch (UnsupportedEncodingException e) {
                //e.printStackTrace();
            }

            Log.e(TAG, "totalScanQtyShow: " + url);
            StringRequest strReq = new StringRequest(Request.Method.GET, url, response -> {
                Log.e(TAG, "onResponse: " + response);
                try {
                    JsonValidator jv = new JsonValidator();
                    JSONObject json = new JSONObject(response);
                    boolean status = jv.getBoolean(json, "Status");

                    if (status) {
                        scanQty = jv.getDouble(json, "ReturnData");
                        double totalQty = scanQty + adjQty;
                        Log.e(TAG, "onResponse: totalQty: " + scanQty);

                        if (scanQty != 0) {
                            edtScanQty.setText(String.valueOf(totalQty));
                        } else {
                            edtScanQty.setText("0");
                        }
                    } else {
                        edtScanQty.setText("0");
                    }

                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }, error -> {
                Log.e(TAG, "totalScanQtyShow: ",error );
            });

            AppController.getInstance().addToRequestQueue(strReq);

        }

    }

    private void getScanItemInfo(final ItemSearch item) {
        String baseUrl = pref.getBaseUrl();

        String deviceId = this.deviceId;
        String userId = this.userId;
        String zoneName = this.zoneName;
        String depoCode = this.outletCode;

        String url = baseUrl + "Data/GetScanItemInfo";
        try {
//				url += "?barcodeItemcode=" + URLEncoder.encode(item.itemCode, getString(R.string.utf_8)) +
//			url += "?barcodeItemcode=" + URLEncoder.encode(pref.getBarCode(), getString(R.string.utf_8)) +
            url += "?barcodeItemcode=" + URLEncoder.encode(item.barcode, getString(R.string.utf_8)) +
                    "&deviceId=" + URLEncoder.encode(deviceId, getString(R.string.utf_8)) +
                    "&userId=" + URLEncoder.encode(userId, getString(R.string.utf_8)) +
                    "&zoneName=" + URLEncoder.encode(zoneName, getString(R.string.utf_8)) +
                    "&depoCode=" + URLEncoder.encode(depoCode, getString(R.string.utf_8));

        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
        }

        Log.e("TAG", "GetItemScanQty url: " + url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, response -> {
            Log.e("TAG", "GetItemScanQty response: " + response);
            try {
                JsonValidator jv = new JsonValidator();
                JSONObject json = new JSONObject(response);
                boolean status = jv.getBoolean(json, "Status");

                if (status) {
                    JSONObject returnObject = json.getJSONObject("ReturnData");

                    adjQty = 0;
                    Log.e(TAG, "onResponse: adjQty: " + returnObject.getDouble("AdjQty"));
                    adjQty = returnObject.getDouble("AdjQty");
                    edtAdjustedQty.setText("" + adjQty);

                    totalScanQtyShow(item);
                } else {
                    edtScanQty.setText("0");
                }

            } catch (JSONException e) {
                //e.printStackTrace();
            }
        }, error -> {
            Log.e(TAG, "getScanItemInfo: ",error );
        });

        AppController.getInstance().addToRequestQueue(strReq);
    }

    private void errorVibration() {
        mVibrator.vibrate(600); // Vibrate for 400 milliseconds
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ITEM_SEARCH) {
            item = (ItemSearch) data.getSerializableExtra("ITEM");
            searchType = TYPE_SEARCH;
            updateLayout(item);
        }
    }

    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_cancel) {
            finish();
        } else if (id == R.id.btn_save) {
            processForSaveItem();
        } else if (id == R.id.btn_clear) {
            clearLayout();
        } else if (id == R.id.btn_barcode_enter) {
            itemSearch();
        }
    }

    private void processForSaveItem() {
        String cqty = "0";
        String rQty = "0";

        final String adjQty = edtAdjustQty.getText().toString().trim();

        if (item == null) {
            Utils.errorDialog(this, "Error", "No Item is selected. Please select a Item first.");
            errorVibration();
            return;
        }

        if (adjQty.isEmpty()) {
            Utils.errorDialog(this, "Empty Quantity!", "Adjust Quantity is empty. Please enter quantity");
            errorVibration();
            return;
        }

        if ((scanQty + Double.parseDouble(adjQty)) < 0) {
            Utils.errorDialog(this, "Adjust Quantity!", "Adjust quantity is not valid.");
            errorVibration();
            return;
        }

        try {
            if (Integer.parseInt(adjQty) == 0) {
                Utils.errorDialog(this, "Invalid Quantity!", "Zero Quantity is not allowed. Please enter positive or negative quantity");
                errorVibration();
                return;
            }
        } catch (Exception ignored) {
        }


        final ItemInventory item = new ItemInventory();
        item.itemCode = this.item.itemCode;
        item.barcode = this.item.barcode;
        item.itemDescription = this.item.name;
        try {
            item.adjQty = Double.parseDouble(adjQty);
        } catch (Exception ignored) {
        }
        item.userId = this.userId;
        item.deviceId = this.deviceId;
        item.zoneName = this.zoneName;
        item.createDate = Utils.getTodayDate();
        item.inventoryDate = Utils.getTodayDate();
        item.scanDate = Utils.getTodayDate();
        item.systemQty = this.item.currentStock;
        item.sQty = this.item.saleQty;
        item.outletCode = this.outletCode;
        item.salePrice = this.item.unitPrice;


        if (item.adjQty == 0) {
            //msg = "Invalid totalQty. please correct it first";
            Utils.errorDialog(this, "", "Invalid totalQty. please correct it first");
        } else if (item.adjQty > -1000 && item.adjQty <= 1000) {
            saveItem(item);
        } else {
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setTitle("Confirmation");
            d.setMessage(Html.fromHtml("<b>" + adjQty + "</b> totalQty is confusing. are you sure to save?"));
            d.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            d.setPositiveButton("YES", (dialog, which) -> saveItem(item));
            d.create().show();
        }

    }

    private void saveItem(final ItemInventory item) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (!pref.isOnlineMode()) {
            progressDialog.dismiss();

            /****************************************
             * save itemInventory to SQLite database
             ***************************************/

            long i = item.save();
            if (i > 0) {
                //successVibration();
                Toast.makeText(AdjustActivity.this, "Data save successfully!", Toast.LENGTH_SHORT).show();
                clearLayout();
            } else {
                errorVibration();
                Utils.errorDialog(AdjustActivity.this, "Error", "Error saving data");
            }

        } else {

            /***************************************
             * call api when online mode is enabled
             **************************************/

            String baseUrl = pref.getBaseUrl();

            String deviceId = this.deviceId;
            String userId = this.userId;
            String zoneName = this.zoneName;
            String depoCode = this.outletCode;

            String url = baseUrl + "Data/UpdateAdjustQty";

            try {
                url += "?Barcode=" + URLEncoder.encode(item.barcode, getString(R.string.utf_8))+
                        "&deviceId="+URLEncoder.encode(deviceId, getString(R.string.utf_8))+
                        "&zoneName="+URLEncoder.encode(zoneName, getString(R.string.utf_8))+
                        "&depoCode="+URLEncoder.encode(depoCode, getString(R.string.utf_8))+
                        "&adjustQty="+URLEncoder.encode(String.valueOf(item.adjQty), getString(R.string.utf_8))+
                        "&userId="+URLEncoder.encode(userId, getString(R.string.utf_8));

            } catch (UnsupportedEncodingException e) {
                //e.printStackTrace();
            }

            Log.e(TAG, "saveItem: adjQty: " + item.adjQty);
            Log.e("TAG", "SaveInventory url: " + url);

            StringRequest strReq = new StringRequest(Request.Method.GET, url, response -> {
                progressDialog.dismiss();

                Log.e("TAG", "response: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    boolean status = json.getBoolean("Status");
                    if (status) {
                        //successVibration();
                        Toast.makeText(AdjustActivity.this, "Data save successfully!", Toast.LENGTH_SHORT).show();
                        clearLayout();
                    } else {
                        errorVibration();
                        Utils.errorDialog(AdjustActivity.this, "Error", "Error saving data");
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }, error -> {
                progressDialog.dismiss();
                errorVibration();
                Utils.errorDialog(AdjustActivity.this, "Connection Error", error.getMessage());
                Log.e("TAG", "Error", error);
            });

            AppController.getInstance().addToRequestQueue(strReq);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //Log.e("TAG", "onKeyDown " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_F4) {
            processForSaveItem(); // save item
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {

        if (edtBarcode.length() > 0) {
            clearLayout();
            return;
        }

        super.onBackPressed();
    }

}
