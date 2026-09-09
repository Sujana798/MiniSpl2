package com.lostandfound.service;

import com.lostandfound.model.ItemReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightedMatchingStrategyTest {

    private final WeightedMatchingStrategy strategy = new WeightedMatchingStrategy();

    private ItemReport buildReport(String type, String category, String title, String description,
                                   String location, String date, String brand, String color) {
        ItemReport report = new ItemReport();
        report.setType(type);
        report.setCategory(category);
        report.setTitle(title);
        report.setDescription(description);
        report.setLocation(location);
        report.setDateOccurred(date);
        report.setBrand(brand);
        report.setColor(color);
        return report;
    }

    @Test
    void identicalReports_shouldProduceHighScore() {
        ItemReport lost = buildReport("LOST", "Electronics", "Black Phone", "Samsung phone lost near library",
                "Library", "2026-01-10", "Samsung", "Black");
        ItemReport found = buildReport("FOUND", "Electronics", "Black Phone", "Samsung phone found near library",
                "Library", "2026-01-10", "Samsung", "Black");

        MatchScoreResult result = strategy.calculateScore(lost, found);

        assertTrue(result.getTotalScore() >= 80.0, "Nearly identical reports should score HIGH");
        assertEquals("HIGH", result.getConfidence());
    }

    @Test
    void differentCategory_shouldScoreZeroOnCategory() {
        ItemReport lost = buildReport("LOST", "Electronics", "Phone", "A phone", "Library", "2026-01-10", null, null);
        ItemReport found = buildReport("FOUND", "Documents", "ID Card", "A card", "Library", "2026-01-10", null, null);

        MatchScoreResult result = strategy.calculateScore(lost, found);

        assertEquals(0.0, result.getCategoryScore());
    }

    @Test
    void completelyUnrelatedReports_shouldScoreLow() {
        ItemReport lost = buildReport("LOST", "Bags", "Blue Backpack", "Lost near gym",
                "Gym", "2026-01-01", "Nike", "Blue");
        ItemReport found = buildReport("FOUND", "Documents", "Student ID", "Found near cafeteria",
                "Cafeteria", "2026-03-01", null, null);

        MatchScoreResult result = strategy.calculateScore(lost, found);

        assertTrue(result.getTotalScore() < 60.0, "Unrelated reports should not score as a strong match");
        assertEquals("LOW", result.getConfidence());
    }

    @Test
    void sameDayReports_shouldScoreFullDateScore() {
        ItemReport lost = buildReport("LOST", "Accessories", "Wallet", "desc", "Library", "2026-02-05", null, null);
        ItemReport found = buildReport("FOUND", "Accessories", "Wallet", "desc", "Library", "2026-02-05", null, null);

        MatchScoreResult result = strategy.calculateScore(lost, found);

        assertEquals(100.0, result.getDateScore());
    }
}