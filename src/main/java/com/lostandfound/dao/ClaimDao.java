package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.Claim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClaimDao {

    public boolean insertClaim(Claim claim) {
        String sql = "INSERT INTO claims (match_id, claimant_id, status) VALUES (?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, claim.getMatchId());
            ps.setInt(2, claim.getClaimantId());
            ps.setString(3, claim.getStatus());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    claim.setClaimId(keys.getInt(1));
                }
            }

            return true;

        } catch (SQLException e) {
            System.out.println("Failed to insert claim: " + e.getMessage());
            return false;
        }
    }

    public boolean existsForMatch(int matchId) {
        String sql = "SELECT COUNT(*) AS total FROM claims WHERE match_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }

        } catch (SQLException e) {
            System.out.println("Failed to check claim existence: " + e.getMessage());
        }

        return false;
    }

    public Claim findByMatchId(int matchId) {
        String sql = "SELECT * FROM claims WHERE match_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToClaim(rs);
            }

        } catch (SQLException e) {
            System.out.println("Failed to find claim by match: " + e.getMessage());
        }

        return null;
    }

    public List<Claim> findByClaimantId(int claimantId) {
        String sql = "SELECT * FROM claims WHERE claimant_id = ? ORDER BY created_at DESC";
        List<Claim> claims = new ArrayList<>();

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimantId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                claims.add(mapResultSetToClaim(rs));
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch claims: " + e.getMessage());
        }

        return claims;
    }

    public List<Claim> findAll() {
        String sql = "SELECT * FROM claims ORDER BY created_at DESC";
        List<Claim> claims = new ArrayList<>();

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                claims.add(mapResultSetToClaim(rs));
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch all claims: " + e.getMessage());
        }

        return claims;
    }

    public boolean updateStatus(int claimId, String status, int reviewedBy) {
        String sql = "UPDATE claims SET status = ?, reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP WHERE claim_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reviewedBy);
            ps.setInt(3, claimId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Failed to update claim status: " + e.getMessage());
            return false;
        }
    }

    private Claim mapResultSetToClaim(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setClaimId(rs.getInt("claim_id"));
        c.setMatchId(rs.getInt("match_id"));
        c.setClaimantId(rs.getInt("claimant_id"));
        c.setStatus(rs.getString("status"));
        int reviewedBy = rs.getInt("reviewed_by");
        c.setReviewedBy(rs.wasNull() ? null : reviewedBy);
        c.setReviewedAt(rs.getString("reviewed_at"));
        c.setCreatedAt(rs.getString("created_at"));
        return c;
    }
}