package com.ms.inventory.utils;


import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.text.Html;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {


	public static AlertDialog errorDialog(Context context, String title, String htmlMsg) {
		return errorDialog(context, title, htmlMsg, false);
	}

	public static AlertDialog errorDialog(Context context, String title, String htmlMsg, boolean isShowIcon) {
		AlertDialog.Builder d = new AlertDialog.Builder(context);

		if (title != null && title.length() > 0)
			d.setTitle(title);

		if (htmlMsg != null && !htmlMsg.isEmpty()) {
			d.setMessage(Html.fromHtml(htmlMsg));
		}

		if (isShowIcon) {
			d.setIcon(android.R.drawable.ic_delete);
		}

		d.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = d.create();
		dialog.show();

		return dialog;
	}


	public static boolean checkPermission(Context context, String permission) {
		int result = ContextCompat.checkSelfPermission(context, permission);
		if (result == PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			return false;
		}
	}

	public static String getTodayDate() {
		//Date date = new Date();
		//String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
		//Log.e("TAG", date);
		return date;

	}

	public static AlertDialog showAlertDialog(Context context, String title, String text){
		AlertDialog.Builder d = new AlertDialog.Builder(context);
		d.setTitle(title);
		d.setMessage(text);
		d.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = d.create();

		dialog.show();
		return dialog;
	}
}
