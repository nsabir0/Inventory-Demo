package com.ms.inventory.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ms.inventory.R;
import com.ms.inventory.adapter.ItemSearchAdapter;
import com.ms.inventory.model.InventoryData;
import com.ms.inventory.model.ItemSearch;
import com.ms.inventory.model.MainItemList;
import com.ms.inventory.utils.AppController;
import com.ms.inventory.utils.DBHelper;
import com.ms.inventory.utils.JsonValidator;
import com.ms.inventory.utils.PreferenceManager;
import com.ms.inventory.utils.Utils;
import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    DBHelper DB;
    PreferenceManager pref;
    private ProgressDialog dialog;

    EditText edtSearch;
    ListView listView;

    List<ItemSearch> list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setTitle("Search Item");

        DB = new DBHelper(SearchActivity.this);
        pref = new PreferenceManager(this);
        dialog = new ProgressDialog(this);

        edtSearch = findViewById(R.id.edt_search);
        listView = findViewById(R.id.listview);



        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchItem();
                    return true;
                }
                return false;
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemSearch item = list.get(position);

                Intent intent = getIntent();
                intent.putExtra("ITEM", item);
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }

    public void onClick(View v) {
        searchItem();
    }

    private void searchItem() {

        if (pref.isOnlineMode()) {
            searchItemForOnline();
        } else {
            //searchItemForOffline();
            new searchItemAsyncTask().execute();
        }
    }

    private void searchItemForOffline() {
        ProgressDialog dialog = ProgressDialog.show(this,null,"Please wait...",false,false);

        String query = edtSearch.getText().toString().trim();
        query = query.toLowerCase();

        list = new ArrayList<>();

        //String sql =  "Select * from MAIN_ITEM_LIST where lower (ITEM_DESCRIPTION) like '%" + query + "%' order by ITEM_DESCRIPTION asc";
        String sql =  "Select * from MAIN_ITEM_LIST " +
                "where (lower (ITEM_DESCRIPTION) like '%" + query + "%' " +
                "or barcode like '%" + query + "%') " +
                "order by ITEM_DESCRIPTION" + " asc";
        Log.e("TAG", "SQL: " + sql);
        List<MainItemList> mainItemLists = SugarRecord.findWithQuery(MainItemList.class,sql);
        //Log.e("TAG", mainItemLists.size() + "");
        for (MainItemList mi : mainItemLists){
            ItemSearch itemSearch = new ItemSearch(mi.itemCode, mi.barcode, mi.itemDescription, mi.stockQty, mi.saleQty, mi.salePrice);
            list.add(itemSearch);
        }

        ItemSearchAdapter adapter = new ItemSearchAdapter(SearchActivity.this, list);
        listView.setAdapter(adapter);

        if (dialog != null){
            dialog.dismiss();
        }

    }

    private void searchItemForOnline() {
//		dialog.show(this,null,"Please wait...",false,false);
        dialog.setMessage("Please wait...");
        dialog.show();

        String query = edtSearch.getText().toString().trim();
        String baseUrl = pref.getBaseUrl();
        String depoCode = pref.getOutletCode();
        String url;
//		String url = baseUrl + "Data/GetByBarcode?barcode=\"\"&depoCode=" + depoCode + "&searchText=" + query;
        //Log.e("TAG", "searchItem URL: "+ url);

//		boolean isReturn = searchResult(url);
//		Log.e("TAG", "searchItemForOnline: isReturn: " + isReturn);

        Log.e(TAG, "searchItemForOnline: " + query);

//		url = baseUrl + "Data/GetByBarcode?barcode=\"\"&depoCode=" + depoCode + "&searchText=" + query;
        url = baseUrl + "Data/GetByBarcode?barcode=&depoCode=" + depoCode + "&searchText=" + query;
        Log.e(TAG, "searchItemForOnline: " + url);
        Log.e(TAG, "searchItemText: " + query);
        searchResult(url);
    }

    private void searchResult(String url) {
//		final boolean[] isReturn = new boolean[1];
//		isReturn[0] = false;

        StringRequest strq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Log.e("TAG", response);
                JsonValidator jv = new JsonValidator();
                list = new ArrayList<>();

                try {
                    JSONObject json = new JSONObject(response);
                    Log.e("TAG", "onResponse: response: " + response);
                    boolean status = jv.getBoolean(json, "Status");

                    if (status) {
                        JSONArray data = json.getJSONArray("ReturnData");

                        if (data.length() > 0) {
//							isReturn[0] = true;

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject j = data.getJSONObject(i);

                                String itemCode = jv.getString(j, "PRODUCTCODE");
                                String name = jv.getString(j, "PRODUCTNAME");
                                double currentStock = jv.getDouble(j, "CurrentStock");
                                double sale = jv.getDouble(j, "SALES");
                                double unitPrice = jv.getDouble(j, "UnitPrice");

                                ItemSearch itemSearch = new ItemSearch(itemCode, itemCode, name, (int) currentStock, (int) sale, unitPrice);
                                list.add(itemSearch);
                            }
                        }
                    }

                    dialog.dismiss();
                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_2,list);
                    ItemSearchAdapter adapter = new ItemSearchAdapter(SearchActivity.this, list);
                    listView.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (dialog != null){
                    dialog.dismiss();
                }
                Log.e("TAG", "Error ", error);
                Utils.errorDialog(SearchActivity.this, "Connection Error", error.getMessage());
            }
        });

        dialog.dismiss();
        AppController.getInstance().addToRequestQueue(strq);
//		return isReturn[0];
    }

    class searchItemAsyncTask extends AsyncTask<String, String, String>{
        ProgressDialog dialog = null;
        String query = edtSearch.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(SearchActivity.this,null,"Please wait...",false,false);
            query = edtSearch.getText().toString().trim();
            query = query.toLowerCase();
            list.clear();
        }

        @Override
        protected String doInBackground(String... params) {
            //String sql = "Select * from MAIN_ITEM_LIST where lower(ITEM_DESCRIPTION) like '%" + query + "%' order by ITEM_DESCRIPTION asc";
            String sql =  "Select * from MAIN_ITEM_LIST " +
                    "where (lower (ITEM_DESCRIPTION) like '%" + query + "%' " +
                    "or barcode like '%" + query + "%') " +
                    "order by ITEM_DESCRIPTION" + " asc";

            List<MainItemList> mainItemLists = SugarRecord.findWithQuery(MainItemList.class, sql);


            final List<InventoryData.Data> item = DB.searchItemsByDescription(query);
            if (!item.isEmpty()) {
                for(InventoryData.Data i : item){
                    MainItemList searchItem = new MainItemList(i.getBarcode(),i.getBarcode(), i.getDescription(), i.getStartQty().intValue(), i.getScanQty().intValue(), i.getMrp());
                    mainItemLists.add(searchItem);}
            }

            //Log.e("TAG", mainItemLists.size() + "");
            for (MainItemList mi : mainItemLists){
                ItemSearch itemSearch = new ItemSearch(mi.itemCode, mi.barcode, mi.itemDescription, mi.stockQty, mi.saleQty, mi.salePrice);
                list.add(itemSearch);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (dialog != null){
                dialog.dismiss();
            }

            ItemSearchAdapter adapter = new ItemSearchAdapter(SearchActivity.this, list);
            listView.setAdapter(adapter);
        }
    }
}
