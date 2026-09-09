package com.lostandfound.service;

import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.dao.UserDao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemReportServiceTest {

    private final MatchService matchService = new MatchService(
            new ItemReportDao(), new MatchDao(), new WeightedMatchingStrategy(), new UserDao());

    private final ItemReportService itemReportService =
            new ItemReportService(new ItemReportDao(), matchService);

    @Test
    void submitReport_withMissingType_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            itemReportService.submitReport(1, "", "Electronics", "Samsung", "Black",
                    "Phone", "desc", "Library", "2026-01-01");
        });

        assertTrue(exception.getMessage().contains("Lost or Found"));
    }

    @Test
    void submitReport_withMissingCategory_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemReportService.submitReport(1, "LOST", "", "Samsung", "Black",
                    "Phone", "desc", "Library", "2026-01-01");
        });
    }

    @Test
    void submitReport_withMissingTitle_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemReportService.submitReport(1, "LOST", "Electronics", "Samsung", "Black",
                    "", "desc", "Library", "2026-01-01");
        });
    }

    @Test
    void submitReport_withMissingLocation_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemReportService.submitReport(1, "LOST", "Electronics", "Samsung", "Black",
                    "Phone", "desc", "", "2026-01-01");
        });
    }

    @Test
    void submitReport_withMissingDate_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemReportService.submitReport(1, "LOST", "Electronics", "Samsung", "Black",
                    "Phone", "desc", "Library", "");
        });
    }
}