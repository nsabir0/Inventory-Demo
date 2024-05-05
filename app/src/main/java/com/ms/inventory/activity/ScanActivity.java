package com.ms.inventory.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ms.inventory.R;
import com.ms.inventory.adapter.ItemSearchAdapter;
import com.ms.inventory.model.InventoryData;
import com.ms.inventory.model.ItemInventory;
import com.ms.inventory.model.ItemSearch;
import com.ms.inventory.model.MainItemList;
import com.ms.inventory.model.SaveInventory;
import com.ms.inventory.model.ScanItems;
import com.ms.inventory.utils.AppController;
import com.ms.inventory.utils.DBHelper;
import com.ms.inventory.utils.JsonValidator;
import com.ms.inventory.utils.PreferenceManager;
import com.ms.inventory.utils.Utils;
import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import mehdi.sakout.fancybuttons.FancyButton;

/** @noinspection deprecation*/
public class ScanActivity extends AppCompatActivity {

	private static final String TAG = "ScanActivity";

	private List<SaveInventory> saveInventoryList;
	private List<ScanItems> scanItemsArrayList;

	TableRow layoutItemCode, layoutScanQty;
	TextView tvDescription, tvItemTotalScanQty, tvTotalScanQty, tvMrp, tvLastScannedItem;
	EditText edtItemCode, edtBarcode, edtStockQty, edtScanQty, edtMultiQty;
	CheckBox chkScanMultiQty;
	FancyButton saveToLocal, btnViewAll;

	DBHelper DB;
	PreferenceManager pref;
	ProgressDialog progressDialog;

	public static final int REQUEST_CODE_ITEM_SEARCH = 100;


	ItemSearch item = null;
	private ItemSearch itemSearch;

	//String barcode = "";
	String scQty = "0", srQty = "0";
	String searchType = "";

	public static final String TYPE_SEARCH = "search";
	public static final String TYPE_SCAN = "barcode";

	private boolean isCameraBarcode = false;
	private String userId, deviceId, zoneName, currentDate, outletCode;
	private double adjQty;

	Vibrator mVibrator;

	private TextWatcher mBarCodeTextWatcher;
	private TextWatcher mQuantityTextWatcher;

	private Timer timer = new Timer();
	private final long DELAY = 100; // in ms

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}

		saveInventoryList = new ArrayList<>();
		scanItemsArrayList = new ArrayList<>();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		DB = new DBHelper(this);
		pref = new PreferenceManager(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Please wait...");
		progressDialog.setCancelable(false);

		edtItemCode = findViewById(R.id.edt_item_code);
		edtBarcode = findViewById(R.id.edt_barcode);
		layoutItemCode = findViewById(R.id.layout_item_code);
		layoutScanQty = findViewById(R.id.layout_scan_qty);
		edtStockQty = findViewById(R.id.edt_stock_qty);
		edtScanQty = this.findViewById(R.id.edt_scan_qty);
		edtMultiQty = findViewById(R.id.edt_scan_qty);
		chkScanMultiQty = findViewById(R.id.chk_multiple_qty);
		tvDescription = findViewById(R.id.tv_description);
		tvItemTotalScanQty = findViewById(R.id.tv_item_total_scan_qty);
		tvTotalScanQty = findViewById(R.id.tv_total_scan_qty);
		tvMrp = findViewById(R.id.tv_mrp);
		tvLastScannedItem = findViewById(R.id.tv_lastScannedItem);

		saveToLocal = findViewById(R.id.btn_save);
		btnViewAll = findViewById(R.id.btn_view_all);

		mBarCodeTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				//				if (s.length() > 5) {
//					itemSearch();
//					edtBarcode.removeTextChangedListener(mBarCodeTextWatcher);
//				}
				if (timer != null) {
					timer.cancel();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				//avoid triggering event when text is too short
				if (s.length() >= 6) {

					timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							// TODO: do what you need here (refresh list)
							// you will probably need to use
							// runOnUiThread(Runnable action) for some specific
							// actions
							itemSearch();
							edtBarcode.removeTextChangedListener(mBarCodeTextWatcher);
						}

					}, DELAY);
				}
			}
		};
		edtBarcode.addTextChangedListener(mBarCodeTextWatcher);

		mQuantityTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 4 && !edtScanQty.isActivated()) {
					errorVibration();
					AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
					builder.setTitle("Quantity!")
							.setMessage("Quantity is too long. Do you want proceed anyway?")
							.setPositiveButton("Yes", (dialog, which) -> {
                                dialog.dismiss();
                                edtScanQty.setActivated(true);
                                edtScanQty.removeTextChangedListener(mQuantityTextWatcher);
                            })
							.setNegativeButton("No", (dialog, which) -> {
                                dialog.dismiss();
                                edtScanQty.setText(null);
//									edtScanQty.setActivated(true);
//									edtScanQty.removeTextChangedListener(mQuantityTextWatcher);
                            })
							.create().show();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		};
		edtScanQty.addTextChangedListener(mQuantityTextWatcher);

		if (pref.isItemCodeVisible()) {
			layoutItemCode.setVisibility(View.VISIBLE);
		} else {
			layoutItemCode.setVisibility(View.GONE);
		}

		if (pref.isStockVisible()) {
			layoutScanQty.setVisibility(View.VISIBLE);
		} else {
			layoutScanQty.setVisibility(View.GONE);
		}

		chkScanMultiQty.setChecked(pref.isMultiScanQty());

		clearLayout();

		this.userId = pref.getUser();
		this.deviceId = pref.getDeviceId();
		this.zoneName = pref.getZoneName();
		this.currentDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(new Date());
		this.outletCode = pref.getOutletCode();

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		chkScanMultiQty.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edtMultiQty.setEnabled(true);
                edtMultiQty.setText("");
            } else {
                edtMultiQty.setEnabled(false);
                edtMultiQty.setText("1");
                edtScanQty.setSelection(edtScanQty.getText().length());
            }
        });

		edtItemCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                isCameraBarcode = false;
                itemSearch();
                return true;
            } else if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                isCameraBarcode = true;
                itemSearch();
                return true;
            }
            return false;
        });

		edtScanQty.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
//					processForSaveItem();
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  // hide the soft keyboard
                }
                return true;
            }

            return false;
        });

		IntentFilter scanDataFilter = new IntentFilter();
		scanDataFilter.addAction("com.jb.action.GET_SCANDATA");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				registerReceiver(scanDataReceiver, scanDataFilter, Context.RECEIVER_EXPORTED);
			}
		}

		btnViewAll.setOnClickListener(v -> startActivity(new Intent(ScanActivity.this, ViewScanItems.class)));

	}

	@Override
	protected void onStart() {
		super.onStart();
		checkTotalScanQty();
		loadLocalSaveItems();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "onActivityResult: ");
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result != null) {
			Log.e("scan", "scan complete");
			if (result.getContents() != null) {
				barcodeProcess(result.getContents());
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}

		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ITEM_SEARCH) {
			Log.e(TAG, "onActivityResult: called");

			item = (ItemSearch) data.getSerializableExtra("ITEM");
			searchType = TYPE_SEARCH;
			updateLayout(item);
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(scanDataReceiver);
		super.onDestroy();
	}

	public void onTopButtonClick(View v) {
		if (v.getId() == R.id.btn_barcode) {
			startBarcodeScanner();
		} else if (v.getId() == R.id.btn_item_search) {
			startActivityForResult(new Intent(this, SearchActivity.class), REQUEST_CODE_ITEM_SEARCH);
		}
	}

	public void loadLocalSaveItems() {
		scanItemsArrayList = new ArrayList<>();
		DB = new DBHelper(ScanActivity.this);

		scanItemsArrayList = DB.readScanItems();

		Log.e(TAG, String.valueOf(scanItemsArrayList.size()));
	}

	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.btn_cancel) {
			finish();
		} else if (id == R.id.btn_save) {
			processForSaveItemToLocal();
			//processForSaveItem();
		} else if (id == R.id.btn_clear) {
			clearLayout();
		} else if (id == R.id.btn_save_to_server) {
			if (scanItemsArrayList != null && !scanItemsArrayList.isEmpty() && !pref.getSession().isEmpty()) {
				finalSaveConfirmation();
			} else if (scanItemsArrayList != null && !scanItemsArrayList.isEmpty() && pref.getSession().isEmpty()) {
				Utils.errorDialog(this, "Empty Session", "Please Login to online mode first to get new session.");
				errorVibration();
			} else {
				Utils.errorDialog(this, "Empty Item", "Scan item is empty. Please scan an item first.");
				errorVibration();
			}
		}
	}

	private void finalSaveConfirmation() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm Save");
		builder.setMessage("Are you sure you want to save items to the server?");

		// Add the buttons
		builder.setPositiveButton("Yes", (dialog, id) -> showPasswordDialog());
		builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());

		AlertDialog dialog = builder.create();
		dialog.show();

	}

	private void showPasswordDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter Password");

		// Set up the input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT
		);
		input.setLayoutParams(params);
		builder.setView(input);

		// Add the buttons
		builder.setPositiveButton("Proceed", (dialog, id) -> {
            String enteredPassword = input.getText().toString();
            String savedPassword = pref.getOfflineUserPassword();

            if (enteredPassword.equals(savedPassword)) {
                dialog.cancel();
                getItemListForServer();
            } else {
                Toast.makeText(getApplicationContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();
            }
        });
		builder.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void clearLayout() {
		isCameraBarcode = false;
		item = null;

		//edtItemCode.setText("");
		edtBarcode.setText("");
		tvDescription.setText("");
		edtStockQty.setText("");
		edtScanQty.setText("");
//		tvItemTotalScanQty.setText("");

		edtBarcode.requestFocus();

		if (chkScanMultiQty.isChecked()) {
			edtScanQty.setText("");
			edtScanQty.setEnabled(true);

		} else {
			edtMultiQty.setText("1");
			edtScanQty.setEnabled(false);
		}

	}

	private void updateLayout(final ItemSearch item) {

		runOnUiThread(new Runnable() {
			@SuppressLint({"DefaultLocale", "SetTextI18n"})
			@Override
			public void run() {
				try {
					if (item != null) {
						Log.e(TAG, "updateLayout: " + item.barcode);
						edtItemCode.setText(item.itemCode);
						edtBarcode.setText(item.barcode);
						tvDescription.setText(item.name);
						edtStockQty.setText(item.currentStock + "");
//						tvMrp.setText(String.format(String.valueOf(item.unitPrice)));
						tvMrp.setText(String.format("%.6f", item.unitPrice));
//            			itemTotalScanQtyShow(item); //show total scan
						getScanItemInfo(item);

						if (!isCameraBarcode) {
							edtScanQty.requestFocus();
//                		edtBarcode.addTextChangedListener(mBarCodeTextWatcher);
						}

						if (searchType.equalsIgnoreCase(TYPE_SCAN)) {
							edtBarcode.addTextChangedListener(mBarCodeTextWatcher);
						}
					}


					if (chkScanMultiQty.isChecked()) {
						edtMultiQty.setText("");
						edtMultiQty.setEnabled(true);
					} else {
						edtMultiQty.setText("1");
						edtMultiQty.setEnabled(false);
					}

					// set cursor at last position
					edtBarcode.setSelection(edtBarcode.getText().length());
					edtScanQty.setSelection(edtScanQty.getText().length());
				} catch (Exception e) {
					Log.e(TAG, "updateLayout error: " + e);
				}
			}
		});
	}

	private void itemSearch() {
		String barcode = edtBarcode.getText().toString();
		Log.e(TAG, "itemSearch: " + barcode);
		if (!barcode.isEmpty()) {
			if (edtScanQty.isActivated()) {
				edtScanQty.setActivated(false);
				edtScanQty.addTextChangedListener(mQuantityTextWatcher);
			}

			pref.setBarCode(barcode);
			barcodeProcess(barcode);
		}
	}

	private void startBarcodeScanner() {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
		integrator.setPrompt("Scan a barcode");
		integrator.setCameraId(0);  // Use a specific camera of the device
		integrator.setBeepEnabled(false);
		integrator.setBarcodeImageEnabled(true);
		integrator.setOrientationLocked(true);
		integrator.initiateScan();
	}

	private void barcodeProcess(String barcode) {
		Log.e("TAG", "Barcode: " + barcode);

		edtBarcode.removeTextChangedListener(mBarCodeTextWatcher);

		if (pref.isOnlineMode()) {
			barcodeProcessForOnline(barcode.trim());
		} else {
			barcodeProcessForOffline(barcode.trim());
		}
	}

	private void barcodeProcessForOffline(final String barcode) {
		try {
			new Thread(() -> {
                final List<ItemSearch> list = new ArrayList<>();
                final InventoryData.Data i = DB.getOfflineItemByBarcode(barcode);
                final List<MainItemList> listItems = SugarRecord.find(MainItemList.class, "barcode=?", barcode);

                if (i != null) {
                    MainItemList searchItem = new MainItemList(i.getBarcode(), barcode, i.getDescription(), i.getStartQty().intValue(), i.getScanQty().intValue(), i.getMrp());
                    listItems.add(searchItem);
                }

                for (MainItemList item : listItems) {
                    ItemSearch is = new ItemSearch(item.itemCode, barcode, item.itemDescription, item.stockQty, item.saleQty, item.salePrice);
                    list.add(is);
                }

                runOnUiThread(() -> {
                    if (list.size() > 1) {
                        //select Item From List
                        showBarcodeMultipleItemDialog(list);
                        //searchType="barcode";
                    } else if (list.size() == 1) {
                        item = list.get(0);
                        searchType = TYPE_SCAN;
                        updateLayout(item);

                        // save for single qty
                        if (!chkScanMultiQty.isChecked()) {
                            //processForSaveItem();
                            processForSaveItemToLocal();
                        }

                    } else {
                        // search Item if barcode process failed
                        barcodeProcessFailed();
                    }
                });
            }).start();
		} catch (Exception e) {
			Log.e(TAG, "barcodeProcessForOffline error: " + e);
		}
	}

	private void barcodeProcessForOnline(final String barcode) {
		Log.e("online", "barcodeProcessForOnline: ");

		final List<ItemSearch> list = new ArrayList<>();

		//barcode = "123";
		String baseUrl = pref.getBaseUrl();
		String depoCode = pref.getOutletCode();
		String url = baseUrl + "Data/GetByBarcode?barcode=" + barcode + "&depoCode=" + depoCode + "&searchText=";
		Log.e("TAG", "GetByBarcode URL: " + url);

		StringRequest strq = new StringRequest(Request.Method.GET, url, response -> {
            Log.e(TAG, response);
            JsonValidator jv = new JsonValidator();

            try {
                JSONObject json = new JSONObject(response);
                boolean status = jv.getBoolean(json, "Status");

                if (status) {
                    JSONArray data = json.getJSONArray("ReturnData");

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject j = data.getJSONObject(i);

                        Log.e(TAG, "onResponse: " + jv.getString(j, "xtype"));
                        if (jv.getString(j, "xtype").equalsIgnoreCase("0")) {
                            edtScanQty.setInputType(InputType.TYPE_CLASS_NUMBER);
                        } else {
                            edtScanQty.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        }

                        String itemCode = jv.getString(j, "PRODUCTCODE");
                        String name = jv.getString(j, "PRODUCTNAME");
                        double currentStock = jv.getDouble(j, "CurrentStock");
                        double sale = jv.getDouble(j, "SALES");
                        double unitPrice = jv.getDouble(j, "UnitPrice");

//							ItemSearch itemSearch = new ItemSearch(itemCode, barcode, name, (int) currentStock, (int) sale, unitPrice);
                        itemSearch = new ItemSearch(itemCode, barcode, name, (int) currentStock, (int) sale, unitPrice);
                        list.add(itemSearch);
                        //Log.e("TAG", code + "  " + name + "  " + sale);
                    }
                }
//					else {
//							Toast.makeText(ScanActivity.this, "not found", Toast.LENGTH_SHORT).show();
//					}

                //Log.e("TAG", list.size()+"  ");
                if (list.size() > 1) {
                    //select Item From List
                    showBarcodeMultipleItemDialog(list);
                    //searchType="barcode";
                } else if (list.size() == 1) {
                    item = list.get(0);
                    searchType = TYPE_SCAN;
                    Log.e("size", "size: " + list.size());
                    updateLayout(item);

                    // save for single qty
                    if (!chkScanMultiQty.isChecked()) {
                        //processForSaveItem();
                        processForSaveItemToLocal();
                    }
                } else {
                    // search Item if barcode process failed
                    barcodeProcessFailed();
                }


            } catch (JSONException e) {
                Log.d(TAG, "barcodeProcessForOnline Jason error :" + e);

            }

        }, error -> {
            //Log.e("TAG", error.getMessage().toString());
            errorVibration();
            Utils.errorDialog(ScanActivity.this, "Connection Error", error.getMessage());
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

		ItemSearchAdapter adapter = new ItemSearchAdapter(ScanActivity.this, list);
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
		edtBarcode.addTextChangedListener(mBarCodeTextWatcher);
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle("Error!");
		d.setMessage("No Item Found. Would you like to search item?");

		d.setNegativeButton("NO", (dialog, which) -> {
            dialog.dismiss();
            clearLayout();
        });
		d.setPositiveButton("YES", (dialog, which) -> startActivityForResult(new Intent(ScanActivity.this, SearchActivity.class), REQUEST_CODE_ITEM_SEARCH));

		d.create().show();
		errorVibration();
	}

	private void getItemListForServer() {

		saveInventoryList.clear();

		SaveInventory saveInventory;

		for (int i = 0; i < scanItemsArrayList.size(); i++) {

			saveInventory = new SaveInventory();

			saveInventory.itemCode = scanItemsArrayList.get(i).getItemCode();
			saveInventory.barcode = scanItemsArrayList.get(i).getBarcode();
			saveInventory.itemDescription = scanItemsArrayList.get(i).getItemDescription();
			saveInventory.scanQty = scanItemsArrayList.get(i).getScanQty();
			saveInventory.adjQty = scanItemsArrayList.get(i).getAdjQty();
			saveInventory.userId = scanItemsArrayList.get(i).getUserId();
			saveInventory.deviceId = scanItemsArrayList.get(i).getDeviceId();
			saveInventory.zoneName = scanItemsArrayList.get(i).getZoneName();
			saveInventory.scQty = scanItemsArrayList.get(i).getScQty();
			saveInventory.srQty = scanItemsArrayList.get(i).getSrQty();
			saveInventory.enQty = scanItemsArrayList.get(i).getEnQty();
			saveInventory.createDate = scanItemsArrayList.get(i).getCreateDate();
			saveInventory.systemQty = scanItemsArrayList.get(i).getSystemQty();
			saveInventory.sQty = scanItemsArrayList.get(i).getsQty();
			saveInventory.outletCode = scanItemsArrayList.get(i).getOutletCode();
			saveInventory.salePrice = scanItemsArrayList.get(i).getSalePrice();

			saveInventoryList.add(saveInventory);
		}

		if (!saveInventoryList.isEmpty()) {
			Log.d("ServerListSize", String.valueOf(saveInventoryList.size()));
			sendItemToServer();
			//saveInventoryList.clear();
		} else {
			//Toast.makeText(this, "No Item here", Toast.LENGTH_SHORT).show();
			Utils.errorDialog(this, "Empty Item", "Scan item is empty. Please scan a item first.");
			errorVibration();
		}
	}

	private void sendItemToServer() {

		String url = pref.getBaseUrl() + "Data/SaveInventory";
		progressDialog.show();

		for (int i = 0; i < saveInventoryList.size(); i++) {

			// Getting Additional Data of item from offline Database
			final InventoryData.Data itemData = DB.getOfflineItemByBarcode(saveInventoryList.get(i).barcode);

			final int finalI = i;
			StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
                progressDialog.dismiss();

                try {
                    JSONObject json = new JSONObject(response);
                    boolean status = json.getBoolean("Status");
                    if (status) {
                        clearLayout();
                        Toast.makeText(ScanActivity.this, "All data save successfully!", Toast.LENGTH_SHORT).show();
                        DB.addUsedSession(pref.getSession());
                        DB.deleteAllData();
                        pref.setSession("");
                        Intent intent = new Intent(ScanActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        //successVibration();
                        //Toast.makeText(ScanActivity.this, "Data save successfully!", Toast.LENGTH_SHORT).show();
                        //itemTotalScanQtyShow(itemSearch);
                    } else {
                        errorVibration();
                        Utils.errorDialog(ScanActivity.this, "Error", "Error saving data");
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "sendItemToServer error: " + e);
                }
            }, error -> {
                progressDialog.dismiss();
                errorVibration();
                Utils.errorDialog(ScanActivity.this, "Connection Error", error.getMessage());
            }) {
				@Override
				protected Map<String, String> getParams() {
					HashMap<String, String> header = new HashMap<>();

					//for (int i = 0; i < saveInventoryList.size(); i++) {
					header.put("UserID", saveInventoryList.get(finalI).userId);
					header.put("SessionId", pref.getSession());
					header.put("DeviceID", saveInventoryList.get(finalI).deviceId);
					header.put("OutletCode", saveInventoryList.get(finalI).outletCode);
					header.put("ZoneName", saveInventoryList.get(finalI).zoneName);
					header.put("ItemCode", saveInventoryList.get(finalI).itemCode);
					header.put("Barcode", saveInventoryList.get(finalI).barcode);
					header.put("sBarcode", itemData == null ? "" : itemData.getsBarcode());
					header.put("User_Barcode", itemData == null ? "" : itemData.getUserBarcode());
					header.put("ItemDescription", saveInventoryList.get(finalI).itemDescription);
					header.put("SalePrice", String.format(Locale.US, "%.6f", Double.parseDouble(saveInventoryList.get(finalI).salePrice)));
					header.put("SystemQty", String.valueOf(saveInventoryList.get(finalI).systemQty));
					header.put("ScanQty", String.valueOf(saveInventoryList.get(finalI).scanQty));
					header.put("ScQty", String.valueOf(saveInventoryList.get(finalI).scQty));
					header.put("AdjQty", String.valueOf(saveInventoryList.get(finalI).adjQty));
					header.put("SrQty", String.valueOf(saveInventoryList.get(finalI).srQty));
					header.put("EnQty", String.valueOf(saveInventoryList.get(finalI).enQty));
					header.put("Sqty", String.valueOf(saveInventoryList.get(finalI).sQty));
					header.put("ScanDate", saveInventoryList.get(finalI).createDate);
					header.put("CPU", String.valueOf(itemData == null ? "" : itemData.getCpu()));

					Log.i("SaveItems", String.valueOf(saveInventoryList.size()));
					Log.e(TAG, "onResponse: Barcode: " + saveInventoryList.get(finalI).barcode);
					Log.e(TAG, "onResponse: Scan Qty: " + saveInventoryList.get(finalI).scanQty);
					Log.e("abir", "onResponse: Scan Qty: " + header);
					//}

					return header;
				}
			};
			AppController.getInstance().addToRequestQueue(strReq);
		}

		checkTotalScanQty();
		loadLocalSaveItems();
		Log.d("saveInventoryList", String.valueOf(scanItemsArrayList.size()));
	}

	private void processForSaveItemToLocal() {

		if (searchType.equalsIgnoreCase(TYPE_SEARCH)) {
			this.scQty = "0";
			this.srQty = edtScanQty.getText().toString().trim();
		} else if (searchType.equalsIgnoreCase(TYPE_SCAN)) {
			this.scQty = edtScanQty.getText().toString().trim();
			this.srQty = "0";
		}

		final String scanQty = edtScanQty.getText().toString().trim();


		if (item == null) {
			Utils.errorDialog(this, "Error", "No Item is selected. Please select a Item first.");
			errorVibration();
			return;
		}

		if (scanQty.isEmpty()) {
			Utils.errorDialog(this, "Empty Quantity!", "Scan Quantity is empty. Please enter quantity");
			errorVibration();
			return;
		}

		try {
			if (Integer.parseInt(scanQty) <= 0) {
				Utils.errorDialog(this, "Zero Quantity!", "Zero Quantity is not allowed. Please enter positive quantity");
				errorVibration();
				return;
			}
		} catch (Exception ignored) {

		}

		final ItemInventory item = new ItemInventory();
		item.itemCode = this.item.itemCode;
		item.barcode = this.item.barcode;
		item.itemDescription = this.item.name;
		item.adjQty = 0;
		item.userId = this.userId;
		item.deviceId = this.deviceId;
		item.zoneName = this.zoneName;
		item.scQty = Double.parseDouble(this.scQty);
		item.srQty = Integer.parseInt(this.srQty);
		item.enQty = 0;
		item.createDate = currentDate;
		item.inventoryDate = currentDate;
		item.systemQty = this.item.currentStock;
		item.sQty = this.item.saleQty;
		item.outletCode = this.outletCode;
		item.salePrice = this.item.unitPrice;
		item.scanQty = Double.parseDouble(scanQty);

		if (scanQty.length() <= 4) {
			saveItemToLocal(item);
		} else {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle("Confirmation");
			d.setMessage(Html.fromHtml("<b>" + scanQty + "</b> qty is confusing. are you sure to save?"));
			d.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
			d.setPositiveButton("YES", (dialog, which) -> saveItemToLocal(item));
			d.create().show();
		}

		tvLastScannedItem.setText(String.format("%s, MRP: %s", item.itemDescription, item.salePrice));

	}

	private void saveItemToLocal(final ItemInventory item) {

		progressDialog.show();

		String itemCode = item.itemCode;
		String barcode = item.barcode;
		String itemDescription = item.itemDescription;
		String scanQty = String.valueOf(item.scanQty);
		String adjQty = String.valueOf(item.adjQty);
		String userId = item.userId;
		String deviceId = item.deviceId;
		String zoneName = item.zoneName;
		String scQty = String.valueOf(item.scQty);
		String srQty = String.valueOf(item.srQty);
		String enQty = String.valueOf(item.enQty);
		String createDate = item.createDate;
		String systemQty = String.valueOf(item.systemQty);
		String sQty = String.valueOf(item.sQty);
		String outletCode = item.outletCode;
		String salePrice = String.valueOf(item.salePrice);

		boolean checkInsertData = DB.addNewItem(itemCode, barcode, itemDescription, scanQty, adjQty, userId, deviceId, zoneName, scQty, srQty, enQty, createDate, systemQty, sQty, outletCode, salePrice);

		itemTotalScanQtyShow();
		checkTotalScanQty();
		loadLocalSaveItems();
		clearLayout();
		progressDialog.dismiss();
		if (checkInsertData) {
			Toast.makeText(ScanActivity.this, "Data save successfully!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(ScanActivity.this, "Data save failed!", Toast.LENGTH_SHORT).show();
		}

	}

	private void checkTotalScanQty() {
		String showTotalScanQty = DB.getTotalScanQty();
		if (showTotalScanQty == null) {
			tvTotalScanQty.setText(R.string._00);
		} else {
			tvTotalScanQty.setText(showTotalScanQty);
		}

		Log.e(TAG, "onResponse: Total Scan Quantity: " + showTotalScanQty);
	}

	private void itemTotalScanQtyShow() {

		String barcode = edtItemCode.getText().toString().trim();
		String showItemTotalScanQty = DB.getItemTotalScanQty(barcode);

		if (showItemTotalScanQty == null) {
			tvItemTotalScanQty.setText(R.string._00);
		} else {
			tvItemTotalScanQty.setText(showItemTotalScanQty);
		}

		Log.e(TAG, "onResponse: Item Total Scan Quantity: " + showItemTotalScanQty);

	}

	private void getScanItemInfo(final ItemSearch item) {
		Log.e(TAG, "ItemSearch item: " + item.toString());

		String baseUrl = pref.getBaseUrl();

		String deviceId = this.deviceId;
		String userId = this.userId;
		String zoneName = this.zoneName;
		String depoCode = this.outletCode;

		String url = baseUrl + "Data/GetScanItemInfo";
		try {
//				url += "?barcodeItemcode=" + URLEncoder.encode(item.itemCode, "UTF-8") +
			url += "?barcodeItemcode=" + URLEncoder.encode(pref.getBarCode(), getString(R.string.utf_8)) +
					"&deviceId=" + URLEncoder.encode(deviceId, getString(R.string.utf_8)) +
					"&userId=" + URLEncoder.encode(userId, getString(R.string.utf_8)) +
					"&zoneName=" + URLEncoder.encode(zoneName, getString(R.string.utf_8)) +
					"&depoCode=" + URLEncoder.encode(depoCode, getString(R.string.utf_8));

		} catch (Exception e) {
			Log.e(TAG, "URL Error: " + e);
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
                    adjQty = returnObject.getDouble("AdjQty");
                    Log.e(TAG, "onResponse: adjQty: " + adjQty);

                    //itemTotalScanQtyShow(item);
                    itemTotalScanQtyShow();
                } else {
                    edtScanQty.setText("0");
                }
            } catch (JSONException e) {
                itemTotalScanQtyShow();
                Log.e(TAG, "onResponse: " + e);
            }
        }, error -> {
            //Log.e("TAG", "Error", error);
            //Utils.errorDialog(ScanActivity.this, "Connection Error", error.getMessage());
        });

		AppController.getInstance().addToRequestQueue(strReq);
	}

	private void errorVibration() {

		mVibrator.vibrate(600); // Vibrate for 400 milliseconds

	}


	private final BroadcastReceiver scanDataReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("TAG", "onReceive: ");
			// Bundle bundle = intent.getExtras();
			String codeString = null;
			String codeId;
			if (intent.hasExtra("data")) {
				codeString = intent.getStringExtra("data");
				Log.e("TAG", "barcode: " + codeString);
			}
			if (intent.hasExtra("codetype")) {
				codeId = intent.getStringExtra("codetype");
				Log.e("TAG", "codeType: " + codeId);

			}

			if (codeString != null) {
				Log.e("bar", "processing barcode....");
				barcodeProcess(codeString);
			}

		}
	};

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//return super.onKeyUp(keyCode, event);
        return switch (keyCode) {
            case KeyEvent.KEYCODE_F1 -> {
                clearLayout();
                yield true;
            }
            case KeyEvent.KEYCODE_F2 -> {
                processForSaveItemToLocal();
                yield true;
                //processForSaveItem(); // save item
            }
            default -> super.onKeyUp(keyCode, event);
        };

	}

}
