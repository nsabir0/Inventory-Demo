package com.ms.inventory.model;

import com.orm.SugarRecord;

public class Configuration extends SugarRecord {

	long configId;
	String serverIp;
	String deviceId;
	String zoneName;
	String outletCode;

	public Configuration() {
	}

	public Configuration(long configId, String serverIp, String deviceId, String zoneName, String outletCode) {
		this.configId = configId;
		this.serverIp = serverIp;
		this.deviceId = deviceId;
		this.zoneName = zoneName;
		this.outletCode = outletCode;
	}
}
