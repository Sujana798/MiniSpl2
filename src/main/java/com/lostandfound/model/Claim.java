package com.lostandfound.model;

public class Claim {
    private int claimId;
    private int matchId;
    private int claimantId;
    private String status;
    private Integer reviewedBy;
    private String reviewedAt;
    private String createdAt;

    public Claim() {
    }

    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public int getClaimantId() { return claimantId; }
    public void setClaimantId(int claimantId) { this.claimantId = claimantId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}