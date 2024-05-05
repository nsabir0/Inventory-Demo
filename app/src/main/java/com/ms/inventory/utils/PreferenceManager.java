package com.ms.inventory.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ms.inventory.model.Configuration;
import com.orm.SugarContext;
import com.orm.SugarRecord;

import java.net.URL;
import java.net.MalformedURLException;

public class PreferenceManager {

	public static final String PREF_NAME = "config";

	SharedPreferences pref;
	//SharedPreferences.Editor editor;

	public static final String PREF_IP = "IP";

	public PreferenceManager(Context context) {
		SugarContext.init(context);
		//pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		pref = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
		//editor = pref.edit();
	}


	public void setUser(String user) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("USER", user);
		editor.apply();
	}

	public String getUser() {
		return pref.getString("USER", "");
	}

	public void setSession(String session) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("session", session);
		editor.apply();
	}

	public String getSession() {
		return pref.getString("session", "");
	}

	public void saveData(String host, String deviceId, String zoneName, String outletCode) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("HOST", host);
		editor.putString("DEVICE_ID", deviceId);
		editor.putString("ZONE_NAME", zoneName);
		editor.putString("OUTLET_CODE", outletCode);
		editor.apply();

		try {
			long count = SugarRecord.count(Configuration.class);

			if (count > 0) {
				SugarRecord.deleteAll(Configuration.class);
			}
		} catch (Exception e) {
			Log.e("Abir001", "save SugarRecord: ", e);
		}

		try {
			Configuration config = new Configuration(1, host, deviceId, zoneName, outletCode);
			config.save();
		} catch (Exception e) {
			Log.e("Abir001", "save Configuration: ", e);
		}


	}

	public void saveOfflineUserInfo(String username, String password) {
//		SharedPreferences loginPreferences = getActivity().getSharedPreferences("offline_reg", 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("user_name", username);
		editor.putString("password", password);
		editor.apply();
	}

	public String getOfflineUserName() {
		return pref.getString("user_name", "");
	}

	public String getOfflineUserPassword() {
		return pref.getString("password", "");
	}


	public String getHost() {
		return pref.getString("HOST", "");
	}

	public void setCounterId(String counterId) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("COUNTER_ID", counterId);
		editor.apply();
	}

	public String getCounterId() {
		return pref.getString("COUNTER_ID", "");
	}

	public String getDeviceId() {
		return pref.getString("DEVICE_ID", "");
	}

	public String getZoneName() {
		return pref.getString("ZONE_NAME", "");
	}

	public String getOutletCode() {
		return pref.getString("OUTLET_CODE", "");
	}

	public boolean isOnlineMode() {
		boolean isOnline = true;
		String mode = pref.getString("pref_key_working_mode", "Online");

		if (!mode.equalsIgnoreCase("Online")) {
			isOnline = false;
		}

		return isOnline;
	}

	public boolean isMultiScanQty() {
		return pref.getBoolean("pref_key_multi_scan_qty", false);
	}

	public boolean isStockVisible() {
		return pref.getBoolean("pref_key_stock_visibility", true);
	}

	public boolean isItemCodeVisible() {
		return pref.getBoolean("pref_key_item_cv", false);
	}

	public void setBarCode(String barCode) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("BAR_CODE", barCode);
		editor.apply();
	}

	public String getBarCode() {
		return pref.getString("BAR_CODE", "");
	}

	public void setMacAddress(String macAddress) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("MAC_ADDRESS", macAddress);
		editor.apply();
	}

	public String getMacAddress() {
		return pref.getString("MAC_ADDRESS", "");
	}

	public String getBaseUrl() {
		String host = getHost();

		try {
			URL url = new URL(host);
			String protocol = url.getProtocol();
			if (protocol == null || (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https"))) {
				host = "http://" + host;
			}

			if (!host.endsWith("/")) {
				host = host + "/";
			}
		} catch (MalformedURLException e) {
			Log.e("GetBaseUrlError", "getBaseUrl Error: ", e);
		}

		return host + "api/";
	}

//	public String getBaseUrl() {
//		//return getHost()+"almasapi/api/";
//		String host = getHost();
//
//		if (!StringUtils.startsWithIgnoreCase(host, "http://") && !StringUtils.startsWithIgnoreCase(host, "https://")) {
//			host = "http://" + host;
//		}
//
//		if (!StringUtils.endsWithIgnoreCase(host, "/")) {
//			host = host + "/";
//		}
//
//		return (host + "api/").trim();
////		return host;
//	}

}