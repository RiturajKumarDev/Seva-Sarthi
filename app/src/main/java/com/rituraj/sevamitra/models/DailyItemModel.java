package com.rituraj.sevamitra.models;

public class DailyItemModel {
    private String id;
    private String itemName;
    private String category;
    private String quantity;
    private String unit;
    private String price;
    private String totalAmount;
    private String date;
    private String time;
    private long timestamp;
    private String supplierId;
    private String status;
    private String notes;

    // Audit Fields
    private String createdBy;
    private String lastUpdatedBy;
    private long lastUpdatedTimestamp;

    // Required empty constructor for Firebase
    public DailyItemModel() {
    }

    // Parameterized constructor
    public DailyItemModel(String id, String itemName, String category,
                          String quantity, String unit, String price,
                          String totalAmount, String date, String time,
                          long timestamp, String supplierId, String status,
                          String notes, String createdBy,
                          String lastUpdatedBy, long lastUpdatedTimestamp) {

        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.totalAmount = totalAmount;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
        this.supplierId = supplierId;
        this.status = status;
        this.notes = notes;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSupplier() {
        return supplierId;
    }

    public void setSupplier(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }
}