package com.lostandfound.service;

import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.model.ItemReport;

public class ItemReportService {

    private final ItemReportDao itemReportDao;

    public ItemReportService(ItemReportDao itemReportDao) {
        this.itemReportDao = itemReportDao;
    }

    public void submitReport(int reporterId, String type, String category, String brand,
                             String color, String title, String description,
                             String location, String dateOccurred) {

        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select whether this is a Lost or Found item.");
        }

        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a category.");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a title for the item.");
        }

        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a location.");
        }

        if (dateOccurred == null || dateOccurred.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a date.");
        }

        ItemReport report = new ItemReport();
        report.setReporterId(reporterId);
        report.setType(type);
        report.setCategory(category);
        report.setBrand(brand);
        report.setColor(color);
        report.setTitle(title.trim());
        report.setDescription(description);
        report.setLocation(location.trim());
        report.setDateOccurred(dateOccurred);
        report.setStatus("REPORTED");

        boolean success = itemReportDao.insertReport(report);
        if (!success) {
            throw new IllegalStateException("Failed to submit report. Please try again.");
        }
    }
}