package com.ms.inventory.model;

import java.io.Serializable;

public class ItemSearch implements Serializable{

	public String itemCode;
	public String barcode;
	public String name;
	public int currentStock;
	public int saleQty;
	public double unitPrice;

	public ItemSearch() {
	}

	public ItemSearch(String itemCode, String name, int currentStock, int saleQty, double unitPrice) {
		this.itemCode = itemCode;
		this.name = name;
		this.currentStock = currentStock;
		this.saleQty = saleQty;
		this.unitPrice = unitPrice;
	}

	public ItemSearch(String itemCode, String barcode, String name, int currentStock, int saleQty, double unitPrice) {
		this.itemCode = itemCode;
		this.barcode = barcode;
		this.name = name;
		this.currentStock = currentStock;
		this.saleQty = saleQty;
		this.unitPrice = unitPrice;
	}
}
