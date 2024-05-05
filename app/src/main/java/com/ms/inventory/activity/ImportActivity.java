package com.ms.inventory.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ms.inventory.R;
import com.ms.inventory.model.ItemInventory;
import com.ms.inventory.model.MainItemList;
import com.ms.inventory.utils.Utils;
import com.orm.SchemaGenerator;
import com.orm.SugarContext;
import com.orm.SugarDb;
import com.orm.SugarRecord;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


/** @noinspection ALL*/
public class ImportActivity extends AppCompatActivity{

	private Uri uriFile;
	boolean isFirstLineHeader;

	private final int RESULT_CODE_CSV = 1015;
	private final int RESULT_CODE_EXCEL = 1025;
	private final int RESULT_CODE_DIRECTORY = 1035;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import);
		Objects.requireNonNull(getSupportActionBar()).setTitle("Import & Export");
	}


	public void onClick(View v) {

		int id = v.getId();
		if (id == R.id.btn_csv) {
			openBrowserForCSVFile();
		} else if (id == R.id.btn_excel) {
			openBrowserForExcel();
		} else if (id == R.id.btn_backup_db) {
			//				addDirectoryChooserFragment();
			openDirectoryPickerForBackup();
		}
	}

	private void openBrowserForCSVFile() {
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle("Import CSV File");
		d.setMessage("Please Pick a CSV file.");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(50, 20, 20, 50);

		final CheckBox checkBox = new CheckBox(this);
		//checkBox.setText("First Line is Header");
		checkBox.setText(R.string.my_data_has_headers);
		checkBox.setChecked(true);
		layout.addView(checkBox);

		d.setView(layout);

		d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				isFirstLineHeader = checkBox.isChecked();

				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				startActivityForResult(intent, RESULT_CODE_CSV);
			}
		});

		d.create().show();


	}

	private void openBrowserForExcel() {
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle("Import from Excel");
		d.setMessage("Please Pick a Excel (.xls) file.");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(50, 20, 20, 50);

		final CheckBox checkBox = new CheckBox(this);
		//checkBox.setText("First Line is Header");
		checkBox.setText(R.string.my_data_has_headers);
		checkBox.setChecked(true);
		layout.addView(checkBox);

		d.setView(layout);

		d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				isFirstLineHeader = checkBox.isChecked();

				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				startActivityForResult(intent, RESULT_CODE_EXCEL);
			}
		});

		d.create().show();


	}

	private void openDirectoryPickerForBackup() {
		Intent intent = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		}
		startActivityForResult(intent, RESULT_CODE_DIRECTORY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			uriFile = data.getData();
			if (uriFile != null) {
				if (requestCode == RESULT_CODE_CSV) {
					new ReadCSV().execute();
				} else if (requestCode == RESULT_CODE_EXCEL) {
					//new ReadExcel().execute();
					readExcelFile();
				} else if (requestCode == RESULT_CODE_DIRECTORY) {
					backupDatabase(String.valueOf(uriFile));
				}
			}
		}
	}

	private void readTextFromUri(Uri uri) throws IOException {

		//List<CsvObject> list = new ArrayList<>();
		List<MainItemList> list = new ArrayList<>();

		InputStream inputStream = getContentResolver().openInputStream(uri);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		//StringBuilder stringBuilder = new StringBuilder();

		String line;
		int i = 0;

		while ((line = reader.readLine()) != null) {

			// skip if it is first line
			if (isFirstLineHeader) {
				isFirstLineHeader = false;
			} else {
				String[] field = line.split(",");

				/*CsvObject obj = new CsvObject(i, row[0], row[1], row[2], row[3], row[4], row[5]);
				list.add(obj);*/

				String itemCode = field[0];
				String barcode = field[1];
				String itemDescription = field[2];
				String stockQty = field[3];
				String saleQty = field[4];
				String salePrice = field[5];

				List<MainItemList> tList = SugarRecord.find(MainItemList.class, "barcode=?", barcode);

				if (tList.isEmpty()) {

					MainItemList item = new MainItemList(itemCode, barcode, itemDescription, stockQty, saleQty, salePrice);
					list.add(item);

					long row = item.save();

					if (row > 0) {
						Log.e("TAG", "Save " + row);
					} else {
						Log.e("TAG", "Not Save " + row);
					}

				} else {

					MainItemList item = tList.get(0);
					item.barcode = barcode;
					item.itemDescription = itemDescription;
					item.stockQty = Integer.parseInt(stockQty);
					item.saleQty = Integer.parseInt(saleQty);
					item.salePrice = Double.parseDouble(salePrice);

					long row = item.save();

					if (row > 0) {
						Log.e("TAG", "update  " + row);
					} else {
						Log.e("TAG", "Not update " + row);
					}

				}

				i++;

				//Log.e("TAG", obj.id + "  " + obj.itemCode);

			}
		}

        if (inputStream != null) {
            inputStream.close();
        }
    }

	private void backupDatabase(final String filePath) {
		File file = new File(filePath);

		if (file.exists()) {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle("File Exist!");
			d.setMessage("File already exists. Are you want to overwrite?");
			d.setNegativeButton("NO", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			d.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					new BackupDatabase().execute(filePath);
					dialog.dismiss();
				}
			});

			d.create().show();

		} else {
			new BackupDatabase().execute(filePath);
		}
	}

	class BackupDatabase extends AsyncTask<String, String, String> {

		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


			pd = new ProgressDialog(ImportActivity.this);
			pd.setTitle("Database Backup");
			//pd.setMessage("Database is updating. Please wait...");
			pd.setMessage("Preparing database...");
			pd.setCancelable(false);
			//pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setIndeterminate(true);
			pd.setProgress(0);
			pd.show();
		}

		@Override
		protected String doInBackground(String... params) {
			File dbFile = getDatabasePath(getString(R.string.DATABASE));
			String outFile = params[0] + File.separator + getString(R.string.DATABASE);

			FileInputStream fis = null;
			OutputStream os = null;

			try {
				fis = new FileInputStream(dbFile);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					os = Files.newOutputStream(Paths.get(outFile));
				}

				int size = fis.available();
				pd.setMax(size);
				pd.setIndeterminate(false);
				pd.setMessage("Please wait, Copping database...");

				// Transfer bytes from the input file to the outfile
				byte[] buffer = new byte[1024];
				int length;
				long copiedBytes = 0;
				while ((length = fis.read(buffer)) > 0) {
					copiedBytes += length;
					if (os != null) {
						os.write(buffer, 0, length);
					}

					//String progress = humanReadableByteCount(alreadyCopied, true);
					publishProgress(String.valueOf(copiedBytes));
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					// Close the streams
					if (os != null) {
						os.flush();
						os.close();
						fis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			//super.onProgressUpdate(values);
			pd.setProgress(Integer.parseInt(values[0]));
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			pd.dismiss();
			ImportActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Utils.errorDialog(ImportActivity.this, "Import Error", "Error found while data has been importing", true);
				}
			});
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			if (pd != null) {
				pd.dismiss();
			}

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			Toast.makeText(ImportActivity.this, "Database Backup completed", Toast.LENGTH_SHORT).show();
			//ImportActivity.this.finish();
		}

	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	class ReadCSV extends AsyncTask<String, String, String> {
		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = new ProgressDialog(ImportActivity.this);
			pd.setMessage("Please wait");
			pd.setCancelable(false);
			pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
			pd.show();
		}

		@Override
		protected String doInBackground(String... params) {

			ContentResolver cR = ImportActivity.this.getContentResolver();
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			String type = mime.getExtensionFromMimeType(cR.getType(uriFile));

			//Log.e("TAG", "type  " + type);

			try {

				if (type.equalsIgnoreCase("csv")) {

					readTextFromUri(uriFile);

				} else {

					ImportActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							String msg = "The file you have selected which is not supported. Please select <b>CSV</b> file.";
							Utils.errorDialog(ImportActivity.this, "File Not Supported!", msg, true);
						}
					});

				}


			} catch (IOException e) {
				//e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			if (pd != null) {
				pd.dismiss();
			}
		}
	}


	private void readExcelFile() {
		ContentResolver cR = ImportActivity.this.getContentResolver();
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		//String type = mime.getExtensionFromMimeType(cR.getType(uriFile));
		String path = uriFile.toString();
		String type = path.substring(path.lastIndexOf(".") + 1);

		/*Log.e("TAG", "type  " + type);
		if (true){
			return;
		}*/

		if (type.equalsIgnoreCase("xls")) {

			new ReadExcel().execute();

		} else {

			final String msg = "The file you have selected which is not supported. Please select <b>Excel 97-2003 (.xls)</b> file.";

			ImportActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Utils.errorDialog(ImportActivity.this, "File Not Supported!", msg, true);
				}
			});

		}
	}


	private void clearDatabase() {
		SugarContext.terminate();
		SchemaGenerator schemaGenerator = new SchemaGenerator(getApplicationContext());
		schemaGenerator.deleteTables(new SugarDb(getApplicationContext()).getDB());
		SugarContext.init(getApplicationContext());
		schemaGenerator.createDatabase(new SugarDb(getApplicationContext()).getDB());
	}


	class ReadExcel extends AsyncTask<String, String, String> {
		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			// delete all row from Main_Item_List
			if (SugarRecord.count(MainItemList.class) > 0) {
				SugarRecord.deleteAll(MainItemList.class);
			}

			// delete all row from Item_inventory
			if (SugarRecord.count(ItemInventory.class) > 0) {
				SugarRecord.deleteAll(ItemInventory.class);
			}


			pd = new ProgressDialog(ImportActivity.this);
			pd.setTitle("Importing...");
			//pd.setMessage("Database is updating. Please wait...");
			pd.setMessage("Preparing excel data...");
			pd.setCancelable(false);
			//pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setIndeterminate(true);
			pd.setProgress(0);
			pd.show();
		}

		@Override
		protected String doInBackground(String... params) {

			clearDatabase(); // remove all data from database;

			InputStream inputStream = null;
			try {
				inputStream = getContentResolver().openInputStream(uriFile);

                assert inputStream != null;
                HSSFWorkbook workbook = new HSSFWorkbook(inputStream); //Get the workbook instance for XLS file
				HSSFSheet sheet = workbook.getSheetAt(0); //Get first sheet from the workbook
				Iterator<Row> rows = sheet.iterator(); //Get iterator to all the rows in current sheet

				final int rowCount = sheet.getLastRowNum();

				ImportActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pd.setMax(rowCount);
						pd.setIndeterminate(false);
						pd.setMessage("Excel data is importing. Please wait...");
					}
				});

				int count = 0;

				while (rows.hasNext()) {

					Row row = rows.next();
					Iterator cells = row.cellIterator();

					// skip line if it is header
					if (isFirstLineHeader) {
						isFirstLineHeader = false;
					} else {
						/*String[] fields = new String[6];

						fields[0] = cells.next().toString();
						fields[1] = cells.next().toString();
						fields[2] = cells.next().toString();
						fields[3] = cells.next().toString();
						fields[4] = cells.next().toString();
						fields[5] = cells.next().toString();

						Log.e("TAG", i+" :  "+fields[0] + "  " + fields[4]);*/

						String itemCode = getValueFromCell((Cell) cells.next());
						String barcode = getValueFromCell((Cell) cells.next());
						String itemDescription = cells.next().toString();
						int stockQty = getIntFromCell((Cell) cells.next());
						int saleQty = getIntFromCell((Cell) cells.next());
						double salePrice = getDoubleFromCell((Cell) cells.next());

						//Log.e("TAG", "Item: "+barcode +"  "+itemDescription);

						MainItemList item = new MainItemList(itemCode, barcode, itemDescription, stockQty, saleQty, salePrice);
						//list.add(item);

						long i = item.save();

						if (i > 0) {
							//Log.e("TAG", "Save " + i);
							count++;
							publishProgress(String.valueOf(count));
						}/* else {
							Log.e("TAG", "Not Save " + i);
						}*/
					}

				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			//super.onProgressUpdate(values);
			pd.setProgress(Integer.parseInt(values[0]));
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			pd.dismiss();
			ImportActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Utils.errorDialog(ImportActivity.this, "Import Error", "Error found while data has been importing", true);
				}
			});
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			if (pd != null) {
				pd.dismiss();
			}

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			Toast.makeText(ImportActivity.this, "Data importing completed", Toast.LENGTH_SHORT).show();
			//ImportActivity.this.finish();
		}
	}

	private String getValueFromCell(Cell cell) {
		String value = "";
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_NUMERIC:
				value = String.valueOf((long) cell.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
		}
		return value;
	}

	private int getIntFromCell(Cell cell) {
		int v = 0;
		try {
			v = (int) cell.getNumericCellValue();
		} catch (Exception e) {
			//Utils.errorDialog(this, "Error parsing", "Cell format error: "+e.getMessage());
		}
		return v;

	}

	private double getDoubleFromCell(Cell cell) {
		double v = 0;
		try {
			v = cell.getNumericCellValue();
		} catch (Exception e) {
			//Utils.errorDialog(this, "Error parsing", "Cell format error: "+e.getMessage());
		}
		return v;

	}

}
