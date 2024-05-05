package com.ms.inventory.model;


public class CsvObject {
	public int id;
	public String itemCode;
	public String barcode;
	public String description;
	public String stockQty;
	public String saleQty;
	public String salePrice;

	public CsvObject() {
	}

	public CsvObject(int id, String itemCode, String barcode, String description, String stockQty, String saleQty, String salePrice) {

		this.id = id;
		this.itemCode = itemCode;
		this.barcode = barcode;
		this.description = description;
		this.stockQty = stockQty;
		this.saleQty = saleQty;
		this.salePrice = salePrice;
	}
}
