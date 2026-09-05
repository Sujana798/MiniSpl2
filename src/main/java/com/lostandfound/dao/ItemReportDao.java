package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.ItemReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemReportDao {

    public boolean insertReport(ItemReport report) {
        String sql = "INSERT INTO item_reports (reporter_id, type, category, brand, color, title, description, location, date_occurred, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, report.getReporterId());
            ps.setString(2, report.getType());
            ps.setString(3, report.getCategory());
            ps.setString(4, report.getBrand());
            ps.setString(5, report.getColor());
            ps.setString(6, report.getTitle());
            ps.setString(7, report.getDescription());
            ps.setString(8, report.getLocation());
            ps.setString(9, report.getDateOccurred());
            ps.setString(10, report.getStatus());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Failed to insert item report: " + e.getMessage());
            return false;
        }
    }

    public List<ItemReport> findByReporterId(int reporterId) {
        String sql = "SELECT * FROM item_reports WHERE reporter_id = ? ORDER BY created_at DESC";
        List<ItemReport> reports = new ArrayList<>();

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reporterId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch reports: " + e.getMessage());
        }

        return reports;
    }

    public List<ItemReport> findAll() {
        String sql = "SELECT * FROM item_reports ORDER BY created_at DESC";
        List<ItemReport> reports = new ArrayList<>();

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch all reports: " + e.getMessage());
        }

        return reports;
    }

    private ItemReport mapResultSetToReport(ResultSet rs) throws SQLException {
        ItemReport report = new ItemReport();
        report.setReportId(rs.getInt("report_id"));
        report.setReporterId(rs.getInt("reporter_id"));
        report.setType(rs.getString("type"));
        report.setCategory(rs.getString("category"));
        report.setBrand(rs.getString("brand"));
        report.setColor(rs.getString("color"));
        report.setTitle(rs.getString("title"));
        report.setDescription(rs.getString("description"));
        report.setLocation(rs.getString("location"));
        report.setDateOccurred(rs.getString("date_occurred"));
        report.setStatus(rs.getString("status"));
        report.setCreatedAt(rs.getString("created_at"));
        return report;
    }
}