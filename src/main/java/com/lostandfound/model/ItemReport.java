package com.lostandfound.model;

import com.lostandfound.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ItemReport {
    private int reportId;
    private int reporterId;
    private String type;
    private String category;
    private String brand;
    private String color;
    private String title;
    private String description;
    private String location;
    private String dateOccurred;
    private String status;
    private String createdAt;

    public ItemReport() {
    }

    public ItemReport(int reportId, int reporterId, String type, String category, String brand,
                      String color, String title, String description, String location,
                      String dateOccurred, String status, String createdAt) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.type = type;
        this.category = category;
        this.brand = brand;
        this.color = color;
        this.title = title;
        this.description = description;
        this.location = location;
        this.dateOccurred = dateOccurred;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDateOccurred() { return dateOccurred; }
    public void setDateOccurred(String dateOccurred) { this.dateOccurred = dateOccurred; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

}