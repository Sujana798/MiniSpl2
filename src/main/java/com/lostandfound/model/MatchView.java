package com.lostandfound.model;

public class MatchView {

    private int matchId;

    private int lostReportId;
    private String lostTitle;
    private String lostCategory;
    private String lostLocation;
    private String lostDate;
    private String lostDescription;
    private String lostBrand;
    private String lostColor;
    private String lostReporterName;

    private int foundReportId;
    private String foundTitle;
    private String foundCategory;
    private String foundLocation;
    private String foundDate;
    private String foundDescription;
    private String foundBrand;
    private String foundColor;
    private String foundReporterName;

    private String category;
    private double matchScore;
    private String confidence;
    private String status;
    private String reason;

    private double categoryScore;
    private double descriptionScore;
    private double locationScore;
    private double dateScore;
    private double attributeScore;

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public int getLostReportId() { return lostReportId; }
    public void setLostReportId(int lostReportId) { this.lostReportId = lostReportId; }

    public String getLostTitle() { return lostTitle; }
    public void setLostTitle(String lostTitle) { this.lostTitle = lostTitle; }

    public String getLostCategory() { return lostCategory; }
    public void setLostCategory(String lostCategory) { this.lostCategory = lostCategory; }

    public String getLostLocation() { return lostLocation; }
    public void setLostLocation(String lostLocation) { this.lostLocation = lostLocation; }

    public String getLostDate() { return lostDate; }
    public void setLostDate(String lostDate) { this.lostDate = lostDate; }

    public String getLostDescription() { return lostDescription; }
    public void setLostDescription(String lostDescription) { this.lostDescription = lostDescription; }

    public String getLostBrand() { return lostBrand; }
    public void setLostBrand(String lostBrand) { this.lostBrand = lostBrand; }

    public String getLostColor() { return lostColor; }
    public void setLostColor(String lostColor) { this.lostColor = lostColor; }

    public String getLostReporterName() { return lostReporterName; }
    public void setLostReporterName(String lostReporterName) { this.lostReporterName = lostReporterName; }

    public int getFoundReportId() { return foundReportId; }
    public void setFoundReportId(int foundReportId) { this.foundReportId = foundReportId; }

    public String getFoundTitle() { return foundTitle; }
    public void setFoundTitle(String foundTitle) { this.foundTitle = foundTitle; }

    public String getFoundCategory() { return foundCategory; }
    public void setFoundCategory(String foundCategory) { this.foundCategory = foundCategory; }

    public String getFoundLocation() { return foundLocation; }
    public void setFoundLocation(String foundLocation) { this.foundLocation = foundLocation; }

    public String getFoundDate() { return foundDate; }
    public void setFoundDate(String foundDate) { this.foundDate = foundDate; }

    public String getFoundDescription() { return foundDescription; }
    public void setFoundDescription(String foundDescription) { this.foundDescription = foundDescription; }

    public String getFoundBrand() { return foundBrand; }
    public void setFoundBrand(String foundBrand) { this.foundBrand = foundBrand; }

    public String getFoundColor() { return foundColor; }
    public void setFoundColor(String foundColor) { this.foundColor = foundColor; }

    public String getFoundReporterName() { return foundReporterName; }
    public void setFoundReporterName(String foundReporterName) { this.foundReporterName = foundReporterName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    /** Convenience getter so the TableView can show "87%" directly. */
    public String getMatchScoreDisplay() {
        return String.format("%.0f%%", matchScore);
    }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

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
}
