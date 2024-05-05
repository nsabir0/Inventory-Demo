package com.ms.inventory.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ms.inventory.R;
import com.ms.inventory.adapter.ScanRVAdapter;
import com.ms.inventory.model.ScanItems;
import com.ms.inventory.utils.DBHelper;

import java.util.ArrayList;

public class ViewScanItems extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static ViewScanItems instance;

    private ArrayList<ScanItems> scanItemsArrayList;
    private DBHelper DB;
    private RecyclerView scanItemsRV;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_scan_items);

        instance = this;

        DB = new DBHelper(this);
        scanItemsArrayList = new ArrayList<>();
        scanItemsRV = findViewById(R.id.idRVScanItems);
        edtSearch = findViewById(R.id.edt_search);
        Button btnSearch = findViewById(R.id.btn_search);

        // Text change listener to the barcode EditText
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchItem(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = edtSearch.getText().toString().toUpperCase().trim();
                searchItem(query);
            }
        });

        loadScanItems();
    }

    public static ViewScanItems getInstance() {
        return instance;
    }

    public void loadScanItems() {
        scanItemsArrayList = DB.readScanItems();
        refreshRecyclerView();
    }

    public void searchItem(String query) {
        // If the query is empty, reload all items
        if (query.isEmpty()) {
            loadScanItems();
        } else {
            // Otherwise, search the item based on barcode
            ScanItems searchedItem = DB.searchScanItems(query.toUpperCase().trim());
            if (searchedItem != null) {
                scanItemsArrayList.clear();
                scanItemsArrayList.add(searchedItem);
                refreshRecyclerView();
            }
        }
    }

    public void updateScanItem(String barcode, String newQuantity) {
        for (ScanItems item : scanItemsArrayList) {
            if (item.getBarcode().equals(barcode)) {
                item.setScanQty(newQuantity);
                break;
            }
        }
        refreshRecyclerView();
    }

    public void refreshRecyclerView() {
        ScanRVAdapter scanRVAdapter = new ScanRVAdapter(scanItemsArrayList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        scanItemsRV.setLayoutManager(linearLayoutManager);
        scanItemsRV.setAdapter(scanRVAdapter);
    }
}
