package com.ms.inventory.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ms.inventory.R;
import com.ms.inventory.adapter.ScanRVAdapter;
import com.ms.inventory.model.ScanItems;
import com.ms.inventory.utils.DBHelper;

import java.util.ArrayList;

public class ViewScanItems extends AppCompatActivity {

    private static ViewScanItems instance;

    private ArrayList<ScanItems> scanItemsArrayList;
    private DBHelper DB;
    private ScanRVAdapter scanRVAdapter;
    private RecyclerView scanItemsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_scan_items);
        instance = this;

        loadScanItems();
    }

    public static ViewScanItems getInstance() {
        return instance;
    }

    public void loadScanItems() {
        scanItemsArrayList = new ArrayList<>();
        DB = new DBHelper(ViewScanItems.this);

        scanItemsArrayList = DB.readScanItems();

        scanRVAdapter = new ScanRVAdapter(scanItemsArrayList, ViewScanItems.this);
        scanItemsRV = (RecyclerView) findViewById(R.id.idRVScanItems);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewScanItems.this, RecyclerView.VERTICAL, false);
        scanItemsRV.setLayoutManager(linearLayoutManager);

        scanItemsRV.setAdapter(scanRVAdapter);
    }

}