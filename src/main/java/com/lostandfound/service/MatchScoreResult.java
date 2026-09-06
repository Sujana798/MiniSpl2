package com.lostandfound.service;

public class MatchScoreResult {

    private final double categoryScore;
    private final double descriptionScore;
    private final double locationScore;
    private final double dateScore;
    private final double attributeScore;
    private final double totalScore;
    private final String confidence;
    private final String reason;

    public MatchScoreResult(double categoryScore, double descriptionScore, double locationScore,
                            double dateScore, double attributeScore, double totalScore,
                            String confidence, String reason) {
        this.categoryScore = categoryScore;
        this.descriptionScore = descriptionScore;
        this.locationScore = locationScore;
        this.dateScore = dateScore;
        this.attributeScore = attributeScore;
        this.totalScore = totalScore;
        this.confidence = confidence;
        this.reason = reason;
    }

    public double getCategoryScore() { return categoryScore; }
    public double getDescriptionScore() { return descriptionScore; }
    public double getLocationScore() { return locationScore; }
    public double getDateScore() { return dateScore; }
    public double getAttributeScore() { return attributeScore; }
    public double getTotalScore() { return totalScore; }
    public String getConfidence() { return confidence; }
    public String getReason() { return reason; }
}
