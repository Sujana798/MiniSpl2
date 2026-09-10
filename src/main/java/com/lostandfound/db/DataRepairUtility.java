package com.lostandfound.db;

import com.lostandfound.dao.ClaimDao;
import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.model.Claim;
import com.lostandfound.model.ItemReport;
import com.lostandfound.model.Match;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class DataRepairUtility {

    private static final String MIGRATION_NAME = "data_repair_v1_report_match_claim_sync";
    private static final String DB_FILE_NAME = "lostandfound.db";


    private static final int RANK_REPORTED = 0;
    private static final int RANK_MATCHED = 1;
    private static final int RANK_CLAIMED = 2;
    private static final int RANK_RETURNED = 3;

    private final ItemReportDao itemReportDao;
    private final MatchDao matchDao;
    private final ClaimDao claimDao;

    private final List<String> repairs = new ArrayList<>();
    private final List<String> flaggedForReview = new ArrayList<>();

    public DataRepairUtility(ItemReportDao itemReportDao, MatchDao matchDao, ClaimDao claimDao) {
        this.itemReportDao = itemReportDao;
        this.matchDao = matchDao;
        this.claimDao = claimDao;
    }


    public static void runOnce() {
        if (alreadyApplied()) {
            System.out.println("Data repair already applied - skipping.");
            return;
        }

        System.out.println("Existing data detected - running one-time report/match/claim reconciliation...");

        backupDatabaseFile();

        DataRepairUtility repair = new DataRepairUtility(new ItemReportDao(), new MatchDao(), new ClaimDao());
        String report = repair.reconcile();

        writeReportFile(report);
        markApplied(report);

        System.out.println(report);
        System.out.println("Data repair complete. Full report written to data_repair_report.txt");
    }



    private static boolean alreadyApplied() {
        String sql = "SELECT 1 FROM app_migrations WHERE migration_name = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, MIGRATION_NAME);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Could not check migration state: " + e.getMessage());
            return false;
        }
    }

    private static void markApplied(String summary) {
        String sql = "INSERT INTO app_migrations (migration_name, summary) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, MIGRATION_NAME);
            ps.setString(2, summary.length() > 4000 ? summary.substring(0, 4000) : summary);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to record migration: " + e.getMessage());
        }
    }

    private static void backupDatabaseFile() {
        try {
            Path source = Paths.get(DB_FILE_NAME);
            if (!Files.exists(source)) {
                System.out.println("No existing database file found at " + source.toAbsolutePath()
                        + " - nothing to back up (fresh database).");
                return;
            }
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path backup = Paths.get(DB_FILE_NAME + ".backup_" + stamp);
            Files.copy(source, backup);
            System.out.println("Database backed up to " + backup.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not back up database before repair - aborting repair for safety: "
                    + e.getMessage(), e);
        }
    }

    private static void writeReportFile(String content) {
        try (PrintWriter writer = new PrintWriter("data_repair_report.txt")) {
            writer.println(content);
        } catch (IOException e) {
            System.out.println("Could not write data_repair_report.txt: " + e.getMessage());
        }
    }

    public String reconcile() {
        List<ItemReport> reports = itemReportDao.findAll();
        List<Match> matches = matchDao.findAll();
        List<Claim> claims = claimDao.findAll();

        Map<Integer, ItemReport> reportsById = new HashMap<>();
        for (ItemReport r : reports) {
            reportsById.put(r.getReportId(), r);
        }

        Map<Integer, List<Claim>> claimsByMatch = new HashMap<>();
        for (Claim c : claims) {
            claimsByMatch.computeIfAbsent(c.getMatchId(), k -> new ArrayList<>()).add(c);
        }

        Map<Integer, String> matchStatusRepairs = new HashMap<>();      // matchId -> new status
        Map<Integer, Integer> reportDesiredRank = new HashMap<>();      // reportId -> max justified rank
        Map<Integer, String> reportEvidence = new HashMap<>();          // reportId -> reason for its rank
        Map<Integer, Boolean> reportHasAnyMatch = new HashMap<>();

        for (Match m : matches) {

            if (m.getLostReportId() == m.getFoundReportId()) {
                if (!"REJECTED".equalsIgnoreCase(m.getStatus())) {
                    repairs.add(String.format(
                            "Match #%d: %s -> REJECTED (invalid self-match: lost_report_id == found_report_id == %d)",
                            m.getMatchId(), m.getStatus(), m.getLostReportId()));
                    matchStatusRepairs.put(m.getMatchId(), "REJECTED");
                }
                continue;
            }

            reportHasAnyMatch.put(m.getLostReportId(), true);
            reportHasAnyMatch.put(m.getFoundReportId(), true);

            List<Claim> matchClaims = claimsByMatch.getOrDefault(m.getMatchId(), List.of());

            int rank;
            String desiredMatchStatus;
            String reason;

            if (hasStatus(matchClaims, "RETURNED")) {
                rank = RANK_RETURNED;
                desiredMatchStatus = "CONFIRMED";
                reason = "linked claim is RETURNED (match #" + m.getMatchId() + ")";
            } else if (hasStatus(matchClaims, "APPROVED")) {
                rank = RANK_CLAIMED;
                desiredMatchStatus = "CONFIRMED";
                reason = "linked claim is APPROVED (match #" + m.getMatchId() + ")";
            } else if (hasStatus(matchClaims, "PENDING")) {
                rank = RANK_MATCHED;
                desiredMatchStatus = "CONFIRMED";
                reason = "linked claim is PENDING (match #" + m.getMatchId() + ")";
            } else if (hasStatus(matchClaims, "REJECTED")) {

                rank = RANK_MATCHED;
                desiredMatchStatus = "CONFIRMED";
                reason = "has a REJECTED claim, so the match must have been CONFIRMED when it was filed (match #"
                        + m.getMatchId() + ")";
            } else if ("CONFIRMED".equalsIgnoreCase(m.getStatus())) {
                rank = RANK_MATCHED;
                desiredMatchStatus = "CONFIRMED";
                reason = "match #" + m.getMatchId() + " is CONFIRMED with no claim yet";
            } else {

                rank = -1;
                desiredMatchStatus = m.getStatus();
                reason = null;
            }

            if (!desiredMatchStatus.equalsIgnoreCase(m.getStatus())) {
                repairs.add(String.format("Match #%d: %s -> %s (%s)",
                        m.getMatchId(), m.getStatus(), desiredMatchStatus, reason));
                matchStatusRepairs.put(m.getMatchId(), desiredMatchStatus);
            }

            if (rank >= 0) {
                applyMaxRank(reportDesiredRank, reportEvidence, m.getLostReportId(), rank, reason);
                applyMaxRank(reportDesiredRank, reportEvidence, m.getFoundReportId(), rank, reason);
            }
        }

        Map<Integer, String> reportStatusRepairs = new HashMap<>();

        for (ItemReport r : reports) {
            int currentRank = rankOf(r.getStatus());
            Integer desiredRank = reportDesiredRank.get(r.getReportId());
            boolean hasMatch = reportHasAnyMatch.getOrDefault(r.getReportId(), false);

            if (desiredRank != null && desiredRank > currentRank) {
                String newStatus = statusOf(desiredRank);
                repairs.add(String.format("Report #%d (\"%s\"): %s -> %s (evidence: %s)",
                        r.getReportId(), r.getTitle(), r.getStatus(), newStatus,
                        reportEvidence.get(r.getReportId())));
                reportStatusRepairs.put(r.getReportId(), newStatus);
            } else if (currentRank > RANK_REPORTED && (desiredRank == null || desiredRank < currentRank)) {
                if (!hasMatch) {
                    flaggedForReview.add(String.format(
                            "Report #%d (\"%s\") is %s but has NO match record referencing it at all - "
                                    + "cannot verify how it reached this status. Left unchanged.",
                            r.getReportId(), r.getTitle(), r.getStatus()));
                } else {
                    flaggedForReview.add(String.format(
                            "Report #%d (\"%s\") is %s but its linked match/claim records only support %s - "
                                    + "left unchanged pending Admin review.",
                            r.getReportId(), r.getTitle(), r.getStatus(), statusOf(Math.max(desiredRank == null ? -1 : desiredRank, RANK_REPORTED))));
                }
            }
        }

        Map<String, List<Integer>> pairToMatchIds = new HashMap<>();
        for (Match m : matches) {
            String key = m.getLostReportId() + ":" + m.getFoundReportId();
            pairToMatchIds.computeIfAbsent(key, k -> new ArrayList<>()).add(m.getMatchId());
        }
        for (Map.Entry<String, List<Integer>> entry : pairToMatchIds.entrySet()) {
            if (entry.getValue().size() > 1) {
                flaggedForReview.add("Duplicate match rows for lost/found pair " + entry.getKey()
                        + ": match IDs " + entry.getValue() + " - review and consolidate manually.");
            }
        }


        if (!matchStatusRepairs.isEmpty() || !reportStatusRepairs.isEmpty()) {
            DatabaseConnection.runInTransaction(() -> {
                for (Map.Entry<Integer, String> e : matchStatusRepairs.entrySet()) {
                    matchDao.updateStatus(e.getKey(), e.getValue());
                }
                for (Map.Entry<Integer, String> e : reportStatusRepairs.entrySet()) {
                    itemReportDao.updateStatus(e.getKey(), e.getValue());
                }
            });
        }

        return buildReportText(reports.size(), matches.size(), claims.size());
    }

    private void applyMaxRank(Map<Integer, Integer> ranks, Map<Integer, String> reasons,
                              int reportId, int rank, String reason) {
        Integer existing = ranks.get(reportId);
        if (existing == null || rank > existing) {
            ranks.put(reportId, rank);
            reasons.put(reportId, reason);
        }
    }

    private boolean hasStatus(List<Claim> claims, String status) {
        for (Claim c : claims) {
            if (status.equalsIgnoreCase(c.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private int rankOf(String status) {
        return switch (status.toUpperCase()) {
            case "REPORTED" -> RANK_REPORTED;
            case "MATCHED" -> RANK_MATCHED;
            case "CLAIMED" -> RANK_CLAIMED;
            case "RETURNED" -> RANK_RETURNED;
            default -> RANK_REPORTED;
        };
    }

    private String statusOf(int rank) {
        return switch (rank) {
            case RANK_MATCHED -> "MATCHED";
            case RANK_CLAIMED -> "CLAIMED";
            case RANK_RETURNED -> "RETURNED";
            default -> "REPORTED";
        };
    }

    private String buildReportText(int reportCount, int matchCount, int claimCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Data Repair Report (").append(MIGRATION_NAME).append(") ===\n");
        sb.append("Scanned ").append(reportCount).append(" item_reports, ")
                .append(matchCount).append(" matches, ").append(claimCount).append(" claims.\n\n");

        sb.append("--- Repairs applied (").append(repairs.size()).append(") ---\n");
        if (repairs.isEmpty()) {
            sb.append("None. Existing data was already consistent.\n");
        } else {
            for (String line : repairs) {
                sb.append("  * ").append(line).append('\n');
            }
        }

        sb.append("\n--- Flagged for manual Admin review (").append(flaggedForReview.size()).append(") ---\n");
        if (flaggedForReview.isEmpty()) {
            sb.append("None.\n");
        } else {
            for (String line : flaggedForReview) {
                sb.append("  ! ").append(line).append('\n');
            }
        }

        return sb.toString();
    }
}
