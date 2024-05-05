// InventoryData.java

package com.ms.inventory.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.List;

public class InventoryData {

    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("TotalPage")
    @Expose
    private Integer totalPage;
    @SerializedName("TotalPageSize")
    @Expose
    private Integer totalPageSize;
    @SerializedName("CurrentPage")
    @Expose
    private Integer currentPage;
    @SerializedName("Data")
    @Expose
    private List<Data> data = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getTotalPageSize() {
        return totalPageSize;
    }

    public void setTotalPageSize(Integer totalPageSize) {
        this.totalPageSize = totalPageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public static class Data  extends SugarRecord{

        @SerializedName("SessionId")
        @Expose
        private String sessionId;
        @SerializedName("Barcode")
        @Expose
        private String barcode;
        @SerializedName("sBarcode")
        @Expose
        private String sBarcode;
        @SerializedName("USER_BARCODE")
        @Expose
        private String userBarcode;
        @SerializedName("StartQty")
        @Expose
        private Double startQty;
        @SerializedName("ScanQty")
        @Expose
        private Double scanQty;
        @SerializedName("ScanStartDate")
        @Expose
        private String scanStartDate;
        @SerializedName("MRP")
        @Expose
        private Double mrp;
        @SerializedName("Description")
        @Expose
        private String description;
        @SerializedName("CPU")
        @Expose
        private Double cpu;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getsBarcode() {
            return sBarcode;
        }

        public void setsBarcode(String sBarcode) {
            this.sBarcode = sBarcode;
        }

        public String getUserBarcode() {
            return userBarcode;
        }

        public void setUserBarcode(String userBarcode) {
            this.userBarcode = userBarcode;
        }

        public Double getStartQty() {
            return startQty;
        }

        public void setStartQty(Double startQty) {
            this.startQty = startQty;
        }

        public Double getScanQty() {
            return scanQty;
        }

        public void setScanQty(Double scanQty) {
            this.scanQty = scanQty;
        }

        public String getScanStartDate() {
            return scanStartDate;
        }

        public void setScanStartDate(String scanStartDate) {
            this.scanStartDate = scanStartDate;
        }

        public Double getMrp() {
            return mrp;
        }

        public void setMrp(Double mrp) {
            this.mrp = mrp;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Double getCpu() {
            return cpu;
        }

        public void setCpu(Double cpu) {
            this.cpu = cpu;
        }

    }
}
