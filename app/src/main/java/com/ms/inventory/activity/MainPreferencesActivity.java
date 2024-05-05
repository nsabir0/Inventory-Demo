package com.ms.inventory.activity;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ms.inventory.R;
import com.ms.inventory.utils.DBHelper;
import com.ms.inventory.utils.Utils;

import android.content.Context;
import android.provider.Settings.Secure;

import java.util.List;
import java.util.Objects;

/**
 * @noinspection ALL
 */
public class MainPreferencesActivity extends AppCompatActivity {

	private static final String TAG = "MainPreferencesActivity";

	public static final String PREF_IP_CONFIG = "pref_key_ip_config";
	public static final String PREF_WORKING_MODE = "pref_key_working_mode";
	public static final String PREF_MULTI_SCAN_QTY = "pref_key_multi_scan_qty";
	public static final String PREF_STOCK_VISIBILITY = "pref_key_stock_visibility";
	public static final String PREF_ITEM_CODE_VISIBILITY = "pref_key_item_cv";
	public static final String PREF_DEVICE_ID = "pref_key_device_id";
	public static final String PREF_SESSION_USED = "pref_key_session_used";

	public static final int PERMISSION_REQUEST_CODE = 105;

	MainPreferenceFragment mainFragment;

	private long pressedTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mainFragment = new MainPreferenceFragment();

		getFragmentManager().beginTransaction().replace(android.R.id.content, mainFragment).commit();
		androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle("Settings");
		}
	}

	public static class MainPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

		DBHelper DB;
		SharedPreferences sharedPref;

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);

			DB = new DBHelper(getActivity());
			sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

			updateWorkingMode();
			updateMultiScanQty();
			updateStockVisibility();
			updateItemCodeVisibility();
			requestDeviceId();

			Preference prefIpConfig = findPreference(PREF_IP_CONFIG);
			prefIpConfig.setOnPreferenceClickListener(this);

		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			if (preference.getKey().equals(PREF_IP_CONFIG)) {
				Intent intent = new Intent(getActivity(), SettingActivity.class);
				startActivity(intent);
				return true;
			}
			return false;
		}


		private void requestDeviceId() {

			if (!Utils.checkPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)) {
				String[] per = {Manifest.permission.READ_PHONE_STATE};
				ActivityCompat.requestPermissions(getActivity(), per, PERMISSION_REQUEST_CODE);
			} else {
				showDeviceId();
			}
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(PREF_WORKING_MODE)) {
				updateWorkingMode();
			} else if (key.equals(PREF_MULTI_SCAN_QTY)) {
				updateMultiScanQty();
			} else if (key.equals(PREF_STOCK_VISIBILITY)) {
				updateStockVisibility();
			} else if (key.equals(PREF_ITEM_CODE_VISIBILITY)) {
				updateItemCodeVisibility();
			}
		}

		private void updateWorkingMode() {
			String prefValue = sharedPref.getString(PREF_WORKING_MODE, "");

			Preference prefMode = findPreference(PREF_WORKING_MODE);
			prefMode.setSummary(prefValue);
		}

		private void updateMultiScanQty() {
			boolean isEnabled = sharedPref.getBoolean(PREF_MULTI_SCAN_QTY, false);
			Preference prefQty = findPreference(PREF_MULTI_SCAN_QTY);

			if (isEnabled) {
				prefQty.setSummary("Enabled");
			} else {
				prefQty.setSummary("Disabled");
			}
		}

		private void updateStockVisibility() {
			boolean isEnabled = sharedPref.getBoolean(PREF_STOCK_VISIBILITY, true);
			Preference prefQty = findPreference(PREF_STOCK_VISIBILITY);

			if (isEnabled) {
				prefQty.setSummary("Show");
			} else {
				prefQty.setSummary("Hide");
			}
		}

		private void updateItemCodeVisibility() {
			boolean isEnabled = sharedPref.getBoolean(PREF_ITEM_CODE_VISIBILITY, false);
			Preference prefQty = findPreference(PREF_ITEM_CODE_VISIBILITY);

			if (isEnabled) {
				prefQty.setSummary("Show");
			} else {
				prefQty.setSummary("Hide");
			}
		}

		private void showDeviceId() {

			String uid = DeviceUtils.getDeviceId(getActivity());
			Preference prefDeviceId = findPreference(PREF_DEVICE_ID);
			prefDeviceId.setSummary(uid);
		}

		public class DeviceUtils {
			public static String getDeviceId(Context context) {
				// Get the Android ID
				String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
				return deviceId;
			}
		}

		private void showSessionId() {
			List<String> sessionIds = DB.getSessionIds();
			Preference prefSessionUsed = findPreference(PREF_SESSION_USED);
			if (sessionIds.isEmpty()) {
				prefSessionUsed.setSummary("No session used before");
			} else {
				String sessionUsed = TextUtils.join("\n", sessionIds);
				prefSessionUsed.setSummary(sessionUsed);
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceScreen().getSharedPreferences()
					.registerOnSharedPreferenceChangeListener(this);
			showSessionId();
		}

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences()
					.unregisterOnSharedPreferenceChangeListener(this);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_CODE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					mainFragment.showDeviceId();
					//Toast.makeText(this, "Permission Granted, Now you can access location data.", Toast.LENGTH_LONG).show();
				} else {
					//Toast.makeText(this, "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();
					errorPermissionDialog();

				}
				break;
		}
	}

	public AlertDialog errorPermissionDialog() {
		AlertDialog.Builder d = new AlertDialog.Builder(this);

		d.setTitle("Permission Denied");

		d.setMessage("Sorry, you denied Phone State permission. Therefore the app cannot " +
				"access device id.\n\nIf you think this is an error, you may need to restart the device.");

		d.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				MainPreferencesActivity.this.finish();
			}
		});
		AlertDialog dialog = d.create();
		dialog.show();

		return dialog;
	}

	@Override
	public void onBackPressed() {
		if (pressedTime + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
			Intent intent = new Intent(MainPreferencesActivity.this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		} else {
			Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
		}
		pressedTime = System.currentTimeMillis();
	}

}
