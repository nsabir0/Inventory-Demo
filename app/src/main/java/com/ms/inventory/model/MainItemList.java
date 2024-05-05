package com.ms.inventory.model;

import com.orm.SugarRecord;


public class MainItemList extends SugarRecord {

	public long id;
	public String itemCode;
	public String barcode;
	public String itemDescription;
	public int stockQty;
	public int saleQty;
	public double salePrice;

	public MainItemList() {
	}

	public MainItemList(String itemCode, String barcode, String itemDescription, int stockQty, int saleQty, double salePrice) {
		this.itemCode = itemCode;
		this.barcode = barcode;
		this.itemDescription = itemDescription;
		this.stockQty = stockQty;
		this.saleQty = saleQty;
		this.salePrice = salePrice;
	}

	public MainItemList(String itemCode, String barcode, String itemDescription, String stockQty, String saleQty, String salePrice) {

		this.itemCode = itemCode;
		this.barcode = barcode;
		this.itemDescription = itemDescription;
		this.stockQty = Integer.parseInt(stockQty);
		this.saleQty = Integer.parseInt(saleQty);
		this.salePrice = Double.parseDouble(salePrice);
	}


}
