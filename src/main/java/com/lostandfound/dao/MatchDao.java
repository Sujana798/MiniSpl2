package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.Match;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDao {

    public Optional<Match> findByPair(int lostReportId, int foundReportId) {
        String sql = "SELECT * FROM matches WHERE lost_report_id = ? AND found_report_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lostReportId);
            ps.setInt(2, foundReportId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMatch(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Failed to find match: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Inserts a new match, or updates the score of an existing Lost-Found
     * pair when recalculated. A pair that an admin already REJECTED is left
     * untouched so recalculation cannot silently un-reject it. This is the
     * single write path used to prevent duplicate Lost-Found match pairs.
     */
    public void saveOrUpdateMatch(Match match) {
        Optional<Match> existing = findByPair(match.getLostReportId(), match.getFoundReportId());

        if (existing.isPresent()) {
            if ("REJECTED".equalsIgnoreCase(existing.get().getStatus())) {
                return;
            }
            updateScore(existing.get().getMatchId(), match);
        } else {
            insertMatch(match);
        }
    }

    private void insertMatch(Match match) {
        String sql = "INSERT INTO matches (lost_report_id, found_report_id, match_score, confidence, status, " +
                "category_score, description_score, location_score, date_score, attribute_score, match_reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, match.getLostReportId());
            ps.setInt(2, match.getFoundReportId());
            ps.setDouble(3, match.getMatchScore());
            ps.setString(4, match.getConfidence());
            ps.setString(5, match.getStatus());
            ps.setDouble(6, match.getCategoryScore());
            ps.setDouble(7, match.getDescriptionScore());
            ps.setDouble(8, match.getLocationScore());
            ps.setDouble(9, match.getDateScore());
            ps.setDouble(10, match.getAttributeScore());
            ps.setString(11, match.getMatchReason());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Failed to insert match: " + e.getMessage());
        }
    }

    private void updateScore(int matchId, Match match) {
        String sql = "UPDATE matches SET match_score = ?, confidence = ?, category_score = ?, " +
                "description_score = ?, location_score = ?, date_score = ?, attribute_score = ?, " +
                "match_reason = ? WHERE match_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, match.getMatchScore());
            ps.setString(2, match.getConfidence());
            ps.setDouble(3, match.getCategoryScore());
            ps.setDouble(4, match.getDescriptionScore());
            ps.setDouble(5, match.getLocationScore());
            ps.setDouble(6, match.getDateScore());
            ps.setDouble(7, match.getAttributeScore());
            ps.setString(8, match.getMatchReason());
            ps.setInt(9, matchId);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Failed to update match: " + e.getMessage());
        }
    }

    public boolean updateStatus(int matchId, String status) {
        String sql = "UPDATE matches SET status = ? WHERE match_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, matchId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Failed to update match status: " + e.getMessage());
            return false;
        }
    }

    public List<Match> findAll() {
        List<Match> matches = new ArrayList<>();
        String sql = "SELECT * FROM matches ORDER BY match_score DESC";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                matches.add(mapResultSetToMatch(rs));
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch matches: " + e.getMessage());
        }

        return matches;
    }

    private Match mapResultSetToMatch(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setMatchId(rs.getInt("match_id"));
        m.setLostReportId(rs.getInt("lost_report_id"));
        m.setFoundReportId(rs.getInt("found_report_id"));
        m.setMatchScore(rs.getDouble("match_score"));
        m.setConfidence(rs.getString("confidence"));
        m.setStatus(rs.getString("status"));
        m.setCategoryScore(rs.getDouble("category_score"));
        m.setDescriptionScore(rs.getDouble("description_score"));
        m.setLocationScore(rs.getDouble("location_score"));
        m.setDateScore(rs.getDouble("date_score"));
        m.setAttributeScore(rs.getDouble("attribute_score"));
        m.setMatchReason(rs.getString("match_reason"));
        m.setCreatedAt(rs.getString("created_at"));
        return m;
    }
}
