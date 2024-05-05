package com.ms.inventory.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ms.inventory.model.InventoryData;
import com.ms.inventory.model.ScanItems;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    private static final String DB_NAME = "inventorydb";

    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "scanitems";

    private static final String INVENTORY_TABLE_NAME = "inventorydata";

    private static final String SESSION_TABLE_NAME = "sessiondata";

    private static final String ID_COL = "id";
    private static final String ITEM_CODE_COL = "itemCode";
    private static final String BARCODE_COL = "barcode";
    private static final String ITEM_DESCRIPTION_COL = "itemDescription";
    private static final String SCAN_QTY_COL = "scanQty";
    private static final String ADJ_QTY_COL = "adjQty";
    private static final String USER_ID_COL = "userId";
    private static final String DEVICE_ID_COL = "deviceId";
    private static final String ZONE_NAME_COL = "zoneName";
    private static final String SC_QTY_COL = "scQty";
    private static final String SR_QTY_COL = "srQty";
    private static final String EN_QTY_COL = "enQty";
    private static final String CREATE_DATE_COL = "createDate";
    private static final String SYSTEM_QTY_COL = "systemQty";
    private static final String S_QTY_COL = "sQty";
    private static final String OUTLET_CODE_COL = "outletCode";
    private static final String SALE_PRICE_COL = "salePrice";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ITEM_CODE_COL + " TEXT,"
                + BARCODE_COL + " TEXT,"
                + ITEM_DESCRIPTION_COL + " TEXT,"
                + SCAN_QTY_COL + " TEXT,"
                + ADJ_QTY_COL + " TEXT,"
                + USER_ID_COL + " TEXT,"
                + DEVICE_ID_COL + " TEXT,"
                + ZONE_NAME_COL + " TEXT,"
                + SC_QTY_COL + " TEXT,"
                + SR_QTY_COL + " TEXT,"
                + EN_QTY_COL + " TEXT,"
                + CREATE_DATE_COL + " TEXT,"
                + SYSTEM_QTY_COL + " TEXT,"
                + S_QTY_COL + " TEXT,"
                + OUTLET_CODE_COL + " TEXT,"
                + SALE_PRICE_COL + " TEXT)";

        // New table creation for inventory data
        String inventoryQuery = "CREATE TABLE " + INVENTORY_TABLE_NAME + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "sessionId TEXT,"
                + "barcode TEXT,"
                + "sBarcode TEXT,"
                + "userBarcode TEXT,"
                + "startQty REAL,"
                + "scanQty REAL,"
                + "scanStartDate TEXT,"
                + "mrp REAL,"
                + "description TEXT,"
                + "cpu REAL)";

        // New table creation for Session data
        String sessionQuery = "CREATE TABLE " + SESSION_TABLE_NAME + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "sessionId TEXT)";

        try {
            db.execSQL(query);
        } catch (Exception e) {
            Log.e("database", "Failed to create scanitems database: " + e);
        }

        try {
            db.execSQL(inventoryQuery);
        } catch (Exception e) {
            Log.e("database", "Failed to create inventoryData database: " + e);
        }

        try {
            db.execSQL(sessionQuery);
        } catch (Exception e) {
            Log.e("database", "Failed to create session database: " + e);
        }
    }

    public interface DataInsertionCallback {
        void onDataInsertionCompleted();
    }

    public int countTableItem() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + INVENTORY_TABLE_NAME, null);
        cursor.moveToFirst();
        int tableItemCount = cursor.getInt(0);
        cursor.close();
        db.endTransaction();

        return tableItemCount;
    }

    public void addUsedSession(String sessionId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put("sessionId", sessionId);
            db.insert(SESSION_TABLE_NAME,null,values);
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.d(TAG, "Error saving saved session"+e);
        }finally {
            db.endTransaction();
        }
    }

    public List<String> getSessionIds() {
        List<String> sessionIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT sessionId FROM " + SESSION_TABLE_NAME, null);


            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String sessionId = cursor.getString(cursor.getColumnIndex("sessionId"));
                    sessionIds.add(sessionId);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("database", "Error fetching session IDs: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return sessionIds;
    }

    public boolean isSessionUsed(String sessionId) {
        boolean isSessionUsed = false ;
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = { "sessionId"};

        String selection = "sessionId = ?";
        String[] selectionArgs = {sessionId};

        Cursor cursor = db.query(
                SESSION_TABLE_NAME,  // The table to query
                projection,            // The array of columns to return (null to return all)
                selection,             // The columns for the WHERE clause
                selectionArgs,         // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // The sort order
        );


        if (cursor != null && cursor.moveToFirst()) {
            isSessionUsed = cursor.getCount() > 0;
            cursor.close();
        }

        return isSessionUsed;
    }

    // Insert inventory data
    public void addInventoryData(List<InventoryData.Data> dataList, DataInsertionCallback callback) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Clear existing data from the table before saving new data
            db.delete(INVENTORY_TABLE_NAME, null, null);

            ContentValues values = new ContentValues();

            for (InventoryData.Data data : dataList) {
                values.put("sessionId", data.getSessionId());
                values.put("barcode", data.getBarcode());
                values.put("sBarcode", data.getsBarcode());
                values.put("userBarcode", data.getUserBarcode());
                values.put("startQty", data.getStartQty());
                values.put("scanQty", data.getScanQty());
                values.put("scanStartDate", data.getScanStartDate());
                values.put("mrp", data.getMrp());
                values.put("description", data.getDescription());
                values.put("cpu", data.getCpu());

                db.insert(INVENTORY_TABLE_NAME, null, values);
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Inventory data inserted successfully.");

        } finally {
            db.endTransaction();
            // Invoke the callback after successful insertion
            if (callback != null) {
                callback.onDataInsertionCompleted();
            }
        }
    }

    public InventoryData.Data getOfflineItemByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                "sessionId",
                "barcode",
                "sBarcode",
                "userBarcode",
                "startQty",
                "scanQty",
                "scanStartDate",
                "mrp",
                "description",
                "cpu"
        };

        String selection = "barcode = ?";
        String[] selectionArgs = {barcode};

        Cursor cursor = db.query(
                INVENTORY_TABLE_NAME,  // The table to query
                projection,            // The array of columns to return (null to return all)
                selection,             // The columns for the WHERE clause
                selectionArgs,         // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null                   // The sort order
        );

        InventoryData.Data inventoryData = null;

        if (cursor != null && cursor.moveToFirst()) {
            inventoryData = new InventoryData.Data();
            inventoryData.setSessionId(cursor.getString(cursor.getColumnIndex("sessionId")));
            inventoryData.setBarcode(cursor.getString(cursor.getColumnIndex("barcode")));
            inventoryData.setsBarcode(cursor.getString(cursor.getColumnIndex("sBarcode")));
            inventoryData.setUserBarcode(cursor.getString(cursor.getColumnIndex("userBarcode")));
            inventoryData.setStartQty((double) cursor.getFloat(cursor.getColumnIndex("startQty")));
            inventoryData.setScanQty((double) cursor.getFloat(cursor.getColumnIndex("scanQty")));
            inventoryData.setScanStartDate(cursor.getString(cursor.getColumnIndex("scanStartDate")));
            inventoryData.setMrp((double) cursor.getFloat(cursor.getColumnIndex("mrp")));
            inventoryData.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            inventoryData.setCpu((double) cursor.getFloat(cursor.getColumnIndex("cpu")));
            cursor.close();
        }

        return inventoryData;
    }

    public List<InventoryData.Data> searchItemsByDescription(String searchString) {
        List<InventoryData.Data> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                "sessionId",
                "barcode",
                "sBarcode",
                "userBarcode",
                "startQty",
                "scanQty",
                "scanStartDate",
                "mrp",
                "description",
                "cpu"
        };

        // Define the selection criteria to search for matches in the description column
        String selection = "description LIKE ?";
        String[] selectionArgs = {"%" + searchString + "%"}; // Include % at the beginning also to search from whole description

        // Query the database to retrieve the first 50 items matching the search criteria
        Cursor cursor = db.query(
                INVENTORY_TABLE_NAME,  // The table to query
                projection,            // The array of columns to return (null to return all)
                selection,             // The columns for the WHERE clause
                selectionArgs,         // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null,                  // Don't order the rows
                "50"                   // Limit the number of results returned to 50
        );

        // Iterate over the cursor to populate the item list
        if (cursor != null) {
            while (cursor.moveToNext()) {
                InventoryData.Data inventoryData = new InventoryData.Data();
                inventoryData.setSessionId(cursor.getString(cursor.getColumnIndex("sessionId")));
                inventoryData.setBarcode(cursor.getString(cursor.getColumnIndex("barcode")));
                inventoryData.setsBarcode(cursor.getString(cursor.getColumnIndex("sBarcode")));
                inventoryData.setUserBarcode(cursor.getString(cursor.getColumnIndex("userBarcode")));
                inventoryData.setStartQty(cursor.getDouble(cursor.getColumnIndex("startQty")));
                inventoryData.setScanQty(cursor.getDouble(cursor.getColumnIndex("scanQty")));
                inventoryData.setScanStartDate(cursor.getString(cursor.getColumnIndex("scanStartDate")));
                inventoryData.setMrp(cursor.getDouble(cursor.getColumnIndex("mrp")));
                inventoryData.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                inventoryData.setCpu(cursor.getDouble(cursor.getColumnIndex("cpu")));
                itemList.add(inventoryData);
            }
            cursor.close();
        }

        return itemList;
    }


    public String getTotalScanQty() {
        String totalScan = "";
        SQLiteDatabase readDB = this.getReadableDatabase();
        Cursor cursorTotalScan = readDB.rawQuery("SELECT SUM(" + DBHelper.SCAN_QTY_COL + ") as scanQty FROM " + DBHelper.TABLE_NAME, null);
        if (cursorTotalScan.moveToFirst()) {
            totalScan = cursorTotalScan.getString(cursorTotalScan.getColumnIndex("scanQty"));
            Log.e(TAG, "onResponse: Total Scan Qty: " + totalScan);
        }
        cursorTotalScan.close();
        return totalScan;
    }

    public String getItemTotalScanQty(String barcode) {

        String oldScanQty = "";

        SQLiteDatabase readDB = this.getReadableDatabase();
        String[] columns = {BARCODE_COL, SCAN_QTY_COL};
        String selection = BARCODE_COL + "=?";
        String[] selectionArgs = new String[1];
        selectionArgs[0] = barcode;
        Cursor cursor = readDB.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();

        if (count > 0) {
            if (cursor.moveToFirst()) {
                do {
                    oldScanQty = cursor.getString(1);
                } while (cursor.moveToNext());
            }

            Log.e(TAG, "onResponse: Count Row: " + count);
            Log.e(TAG, "onResponse: Old Scan Quantity: " + oldScanQty);

        } else {
            Log.e(TAG, "onResponse: Count Row: " + count);
            oldScanQty = "00";

        }
        cursor.close();
        readDB.close();
        return oldScanQty;
    }

    public boolean addNewItem(String itemCode, String barcode, String itemDescription, String scanQty, String adjQty, String userId, String deviceId, String zoneName, String scQty, String srQty, String enQty, String createDate, String systemQty, String sQty, String outletCode, String salePrice) {

        ContentValues values = new ContentValues();

        values.put(ITEM_CODE_COL, itemCode);
        values.put(BARCODE_COL, barcode);
        values.put(ITEM_DESCRIPTION_COL, itemDescription);
        values.put(SCAN_QTY_COL, scanQty);
        values.put(ADJ_QTY_COL, adjQty);
        values.put(USER_ID_COL, userId);
        values.put(DEVICE_ID_COL, deviceId);
        values.put(ZONE_NAME_COL, zoneName);
        values.put(SC_QTY_COL, scQty);
        values.put(SR_QTY_COL, srQty);
        values.put(EN_QTY_COL, enQty);
        values.put(CREATE_DATE_COL, createDate);
        values.put(SYSTEM_QTY_COL, systemQty);
        values.put(S_QTY_COL, sQty);
        values.put(OUTLET_CODE_COL, outletCode);
        values.put(SALE_PRICE_COL, salePrice);

        SQLiteDatabase readDB = this.getReadableDatabase();
        SQLiteDatabase writeDB = this.getWritableDatabase();

        String[] columns = {BARCODE_COL, SCAN_QTY_COL};
        String selection = BARCODE_COL + "=?";
        String[] selectionArgs = new String[1];
        selectionArgs[0] = barcode;
        Cursor cursor = readDB.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();

        if (count > 0) {
            String oldScanQty = "";
            if (cursor.moveToFirst()) {
                do {
                    oldScanQty = cursor.getString(1);
                } while (cursor.moveToNext());
            }

            double newScanQty = Double.parseDouble(oldScanQty) + Double.parseDouble(scanQty);
            String newScanQtyStr = String.valueOf(newScanQty);

            ContentValues scanValue = new ContentValues();
            scanValue.put(SCAN_QTY_COL, newScanQtyStr);
            long result = writeDB.update(TABLE_NAME, scanValue, "barcode=?", new String[]{barcode});

            Log.e(TAG, "onResponse: Count Row: " + count);
            Log.e(TAG, "onResponse: Old Scan Quantity: " + oldScanQty);
            Log.e(TAG, "onResponse: Total Scan Quantity: " + newScanQtyStr);

            cursor.close();
            readDB.close();
            writeDB.close();

            if (result > 0) {
                Log.e(TAG, "onResponse: Data update successfully!");
                return true;
            } else {
                Log.e(TAG, "onResponse: Data update failed!");
                return false;
            }

        } else {
            Log.e(TAG, "onResponse: Count Row: " + count);
            long result = writeDB.insert(TABLE_NAME, null, values);

            cursor.close();
            readDB.close();
            writeDB.close();

            if (result > 0) {
                Log.e(TAG, "onResponse: Data save successfully!");
                return true;
            } else {
                Log.e(TAG, "onResponse: Data save failed!");
                return false;
            }
        }

    }

    public ArrayList<ScanItems> readScanItems() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursorScanItem = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        ArrayList<ScanItems> scanItemsArrayList = new ArrayList<>();

        if (cursorScanItem.moveToFirst()) {
            do {
                scanItemsArrayList.add(new ScanItems(
                        cursorScanItem.getString(1),
                        cursorScanItem.getString(2),
                        cursorScanItem.getString(3),
                        cursorScanItem.getString(4),
                        cursorScanItem.getString(5),
                        cursorScanItem.getString(6),
                        cursorScanItem.getString(7),
                        cursorScanItem.getString(8),
                        cursorScanItem.getString(9),
                        cursorScanItem.getString(10),
                        cursorScanItem.getString(11),
                        cursorScanItem.getString(12),
                        cursorScanItem.getString(13),
                        cursorScanItem.getString(14),
                        cursorScanItem.getString(15),
                        cursorScanItem.getString(16)));
            } while (cursorScanItem.moveToNext());
        }
        Log.e(TAG, String.valueOf(scanItemsArrayList.size()));
        cursorScanItem.close();
        return scanItemsArrayList;
    }

    public void updateScanItem(String barcode, String scanQty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(SCAN_QTY_COL, scanQty);

        db.update(TABLE_NAME, values, "barcode=?", new String[]{barcode});
        db.close();
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(INVENTORY_TABLE_NAME, null, null);
        long result = db.delete(TABLE_NAME, null, null);
        db.close();

        if (result > 0) {
            Log.e(TAG, "onResponse: Data delete successfully!");
        } else {
            Log.e(TAG, "onResponse: Data delete failed!");
        }
    }

    public boolean deleteSingleData(String barcode) {

        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(TABLE_NAME, "barcode=?", new String[]{barcode});
        db.close();

        if (result > 0) {
            Log.e(TAG, "onResponse: Item delete successfully!");
            return true;
        } else {
            Log.e(TAG, "onResponse: Item delete failed!");
            return false;
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}