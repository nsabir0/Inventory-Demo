package com.ms.inventory.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ms.inventory.R;
import com.ms.inventory.activity.UpdateScanItemActivity;
import com.ms.inventory.model.ScanItems;

import java.util.ArrayList;

public class ScanRVAdapter extends RecyclerView.Adapter<ScanRVAdapter.ViewHolder> {

    private final ArrayList<ScanItems> scanItemsArrayList;
    private final Context context;

    public ScanRVAdapter(ArrayList<ScanItems> scanItemsArrayList, Context context) {
        this.scanItemsArrayList = scanItemsArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final ScanItems modal = scanItemsArrayList.get(position);
        holder.barcodeTV.setText(modal.getBarcode());
        holder.itemDescriptionTV.setText(modal.getItemDescription());
        holder.scanQtyTV.setText(modal.getScanQty());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, UpdateScanItemActivity.class);

                i.putExtra("barcode", modal.getBarcode());
                i.putExtra("itemDescription", modal.getItemDescription());
                i.putExtra("scanQty", modal.getScanQty());

                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return scanItemsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView barcodeTV;
        private final TextView itemDescriptionTV;
        private final TextView scanQtyTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            barcodeTV = itemView.findViewById(R.id.idTVBarcode);
            itemDescriptionTV = itemView.findViewById(R.id.idTVItemDescription);
            scanQtyTV = itemView.findViewById(R.id.idTVScanQty);
        }
    }
}
