package com.lostandfound.service;

import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.dao.UserDao;
import com.lostandfound.model.ItemReport;
import com.lostandfound.model.Match;
import com.lostandfound.model.MatchView;
import com.lostandfound.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MatchService {


    private static final double MIN_SCORE_TO_STORE = 60.0;
    private final ItemReportDao itemReportDao;
    private final MatchDao matchDao;
    private final MatchingStrategy matchingStrategy;
    private final UserDao userDao;

    public MatchService(ItemReportDao itemReportDao, MatchDao matchDao,
                        MatchingStrategy matchingStrategy, UserDao userDao) {
        this.itemReportDao = itemReportDao;
        this.matchDao = matchDao;
        this.matchingStrategy = matchingStrategy;
        this.userDao = userDao;
    }


    public void generateMatchesForReport(ItemReport newReport) {
        if (newReport == null || !isMatchable(newReport)) {
            return;
        }

        String oppositeType = "LOST".equalsIgnoreCase(newReport.getType()) ? "FOUND" : "LOST";

        List<ItemReport> candidates = itemReportDao.findAll().stream()
                .filter(r -> oppositeType.equalsIgnoreCase(r.getType()))
                .filter(this::isMatchable)
                .collect(Collectors.toList());

        for (ItemReport candidate : candidates) {
            evaluateAndStore(newReport, candidate);
        }
    }


    public void recalculateAllMatches() {
        List<ItemReport> all = itemReportDao.findAll();

        List<ItemReport> lostReports = all.stream()
                .filter(r -> "LOST".equalsIgnoreCase(r.getType()))
                .filter(this::isMatchable)
                .collect(Collectors.toList());

        List<ItemReport> foundReports = all.stream()
                .filter(r -> "FOUND".equalsIgnoreCase(r.getType()))
                .filter(this::isMatchable)
                .collect(Collectors.toList());

        for (ItemReport lost : lostReports) {
            for (ItemReport found : foundReports) {
                evaluateAndStore(lost, found);
            }
        }
    }

    public void rejectMatch(int matchId) {
        matchDao.updateStatus(matchId, "REJECTED");
    }

    public List<MatchView> getAllMatchViews() {
        List<Match> matches = matchDao.findAll();
        List<MatchView> views = new ArrayList<>();

        for (Match m : matches) {
            ItemReport lost = itemReportDao.findById(m.getLostReportId());
            ItemReport found = itemReportDao.findById(m.getFoundReportId());
            if (lost == null || found == null) {
                continue; //
            }
            views.add(buildMatchView(m, lost, found));
        }

        return views;
    }


    private boolean isMatchable(ItemReport report) {
        String status = report.getStatus();
        return "REPORTED".equalsIgnoreCase(status) || "MATCHED".equalsIgnoreCase(status);
    }

    private void evaluateAndStore(ItemReport reportA, ItemReport reportB) {
        ItemReport lost = "LOST".equalsIgnoreCase(reportA.getType()) ? reportA : reportB;
        ItemReport found = "FOUND".equalsIgnoreCase(reportA.getType()) ? reportA : reportB;

        if (lost.getReportId() == found.getReportId()) {
            return;
        }

        MatchScoreResult result = matchingStrategy.calculateScore(lost, found);

        if (result.getTotalScore() < MIN_SCORE_TO_STORE) {
            return;
        }

        Match match = new Match();
        match.setLostReportId(lost.getReportId());
        match.setFoundReportId(found.getReportId());
        match.setMatchScore(result.getTotalScore());
        match.setConfidence(result.getConfidence());
        match.setCategoryScore(result.getCategoryScore());
        match.setDescriptionScore(result.getDescriptionScore());
        match.setLocationScore(result.getLocationScore());
        match.setDateScore(result.getDateScore());
        match.setAttributeScore(result.getAttributeScore());
        match.setMatchReason(result.getReason());
        match.setStatus("PENDING");

        matchDao.saveOrUpdateMatch(match);
    }

    private MatchView buildMatchView(Match m, ItemReport lost, ItemReport found) {
        MatchView v = new MatchView();

        v.setMatchId(m.getMatchId());

        v.setLostReportId(lost.getReportId());
        v.setLostTitle(lost.getTitle());
        v.setLostCategory(lost.getCategory());
        v.setLostLocation(lost.getLocation());
        v.setLostDate(lost.getDateOccurred());
        v.setLostDescription(lost.getDescription());
        v.setLostBrand(lost.getBrand());
        v.setLostColor(lost.getColor());
        v.setLostReporterName(resolveReporterName(lost.getReporterId()));

        v.setFoundReportId(found.getReportId());
        v.setFoundTitle(found.getTitle());
        v.setFoundCategory(found.getCategory());
        v.setFoundLocation(found.getLocation());
        v.setFoundDate(found.getDateOccurred());
        v.setFoundDescription(found.getDescription());
        v.setFoundBrand(found.getBrand());
        v.setFoundColor(found.getColor());
        v.setFoundReporterName(resolveReporterName(found.getReporterId()));

        v.setCategory(lost.getCategory());
        v.setMatchScore(m.getMatchScore());
        v.setConfidence(m.getConfidence());
        v.setStatus(m.getStatus());
        v.setReason(m.getMatchReason());

        v.setCategoryScore(m.getCategoryScore());
        v.setDescriptionScore(m.getDescriptionScore());
        v.setLocationScore(m.getLocationScore());
        v.setDateScore(m.getDateScore());
        v.setAttributeScore(m.getAttributeScore());

        return v;
    }

    private String resolveReporterName(int userId) {
        User user = userDao.findById(userId);
        return user != null ? user.getName() : "Unknown";
    }
}
