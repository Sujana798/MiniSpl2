package com.lostandfound.model;

public class Match {

    private int matchId;
    private int lostReportId;
    private int foundReportId;
    private double matchScore;
    private String confidence;   // "HIGH", "MEDIUM", "LOW"
    private String status;       // "PENDING", "CONFIRMED", "REJECTED"

    private double categoryScore;
    private double descriptionScore;
    private double locationScore;
    private double dateScore;
    private double attributeScore;

    private String matchReason;
    private String createdAt;

    public Match() {
    }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public int getLostReportId() { return lostReportId; }
    public void setLostReportId(int lostReportId) { this.lostReportId = lostReportId; }

    public int getFoundReportId() { return foundReportId; }
    public void setFoundReportId(int foundReportId) { this.foundReportId = foundReportId; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCategoryScore() { return categoryScore; }
    public void setCategoryScore(double categoryScore) { this.categoryScore = categoryScore; }

    public double getDescriptionScore() { return descriptionScore; }
    public void setDescriptionScore(double descriptionScore) { this.descriptionScore = descriptionScore; }

    public double getLocationScore() { return locationScore; }
    public void setLocationScore(double locationScore) { this.locationScore = locationScore; }

    public double getDateScore() { return dateScore; }
    public void setDateScore(double dateScore) { this.dateScore = dateScore; }

    public double getAttributeScore() { return attributeScore; }
    public void setAttributeScore(double attributeScore) { this.attributeScore = attributeScore; }

    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
