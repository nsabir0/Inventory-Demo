package com.ms.inventory.model;

import com.orm.SugarRecord;


public class ItemInventory extends SugarRecord {

	public long id;
	public int invDetailsId=0;	// not required
	public int invId=0;			// not required
	public String invNo=""; 		// not required
	public String itemCode="";
	public String itemDescription="";
	public int systemQty=0;
	public double scanQty=0;
	public double adjQty=0;		// not required
	public String createDate="";
	public String scanDate="";
	public String inventoryDate="";
	public String userId="";
	public String deviceId="";
	public int sQty=0;			// not required
	public double salePrice=0; 	// not required
	public String barcode="";
	public double scQty=0;
	public int srQty=0;
	public int enQty=0;			// not required
	public String zoneName="";
	public String outletCode="";
	public String saleDateRange="";	// not required
	public boolean isTransfer=false; 	// not required


//	public String itemCode="";
//	public String barcode="";
//	public String itemDescription="";
//	public double scanQty=0;
//	public double adjQty=0;		// not required
//	public String userId="";
//	public String deviceId="";
//	public String zoneName="";
//	public double scQty=0;
//	public int srQty=0;
//	public int enQty=0;			// not required
//	public String createDate="";
//	public int systemQty=0;
//	public int sQty=0;			// not required
//	public String outletCode="";
//	public double salePrice=0; 	// not required


	public ItemInventory() {
	}
}
