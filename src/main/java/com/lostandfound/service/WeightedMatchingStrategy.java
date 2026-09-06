package com.lostandfound.service;

import com.lostandfound.model.ItemReport;
import com.lostandfound.util.TextSimilarityUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public class WeightedMatchingStrategy implements MatchingStrategy {

    public static final double CATEGORY_WEIGHT = 0.25;
    public static final double DESCRIPTION_WEIGHT = 0.30;
    public static final double LOCATION_WEIGHT = 0.20;
    public static final double DATE_WEIGHT = 0.15;
    public static final double ATTRIBUTE_WEIGHT = 0.10;

    private static final double HIGH_CONFIDENCE_THRESHOLD = 80.0;
    private static final double MEDIUM_CONFIDENCE_THRESHOLD = 60.0;

    @Override
    public MatchScoreResult calculateScore(ItemReport lost, ItemReport found) {
        double categoryScore = calculateCategoryScore(lost, found);
        double descriptionScore = calculateDescriptionScore(lost, found);
        double locationScore = calculateLocationScore(lost, found);
        double dateScore = calculateDateScore(lost, found);
        double attributeScore = calculateAttributeScore(lost, found);

        double total = categoryScore * CATEGORY_WEIGHT
                + descriptionScore * DESCRIPTION_WEIGHT
                + locationScore * LOCATION_WEIGHT
                + dateScore * DATE_WEIGHT
                + attributeScore * ATTRIBUTE_WEIGHT;

        String confidence = classifyConfidence(total);
        String reason = buildReason(categoryScore, descriptionScore, locationScore, dateScore, attributeScore, lost);

        return new MatchScoreResult(categoryScore, descriptionScore, locationScore, dateScore,
                attributeScore, total, confidence, reason);
    }

    private double calculateCategoryScore(ItemReport lost, ItemReport found) {
        if (isBlank(lost.getCategory()) || isBlank(found.getCategory())) {
            return 0.0;
        }
        return lost.getCategory().trim().equalsIgnoreCase(found.getCategory().trim()) ? 100.0 : 0.0;
    }

    private double calculateDescriptionScore(ItemReport lost, ItemReport found) {
        String lostText = safe(lost.getTitle()) + " " + safe(lost.getDescription());
        String foundText = safe(found.getTitle()) + " " + safe(found.getDescription());

        Set<String> lostTokens = TextSimilarityUtil.tokenize(lostText);
        Set<String> foundTokens = TextSimilarityUtil.tokenize(foundText);

        return TextSimilarityUtil.jaccardSimilarity(lostTokens, foundTokens) * 100.0;
    }

    private double calculateLocationScore(ItemReport lost, ItemReport found) {
        String lostLoc = TextSimilarityUtil.normalize(lost.getLocation());
        String foundLoc = TextSimilarityUtil.normalize(found.getLocation());

        if (lostLoc.isEmpty() || foundLoc.isEmpty()) {
            return 0.0;
        }
        if (lostLoc.equals(foundLoc)) {
            return 100.0;
        }
        if (lostLoc.contains(foundLoc) || foundLoc.contains(lostLoc)) {
            return 75.0;
        }

        Set<String> lostTokens = TextSimilarityUtil.tokenize(lost.getLocation());
        Set<String> foundTokens = TextSimilarityUtil.tokenize(found.getLocation());
        return TextSimilarityUtil.jaccardSimilarity(lostTokens, foundTokens) * 100.0;
    }

    private double calculateDateScore(ItemReport lost, ItemReport found) {
        LocalDate lostDate = parseDate(lost.getDateOccurred());
        LocalDate foundDate = parseDate(found.getDateOccurred());

        if (lostDate == null || foundDate == null) {
            return 0.0;
        }

        long daysApart = Math.abs(ChronoUnit.DAYS.between(lostDate, foundDate));

        if (daysApart == 0) return 100.0;
        if (daysApart <= 2) return 90.0;
        if (daysApart <= 5) return 70.0;
        if (daysApart <= 10) return 50.0;
        if (daysApart <= 20) return 30.0;
        return 0.0;
    }

    private double calculateAttributeScore(ItemReport lost, ItemReport found) {
        int comparableAttributes = 0;
        int matchedAttributes = 0;

        if (!isBlank(lost.getBrand()) && !isBlank(found.getBrand())) {
            comparableAttributes++;
            if (lost.getBrand().trim().equalsIgnoreCase(found.getBrand().trim())) {
                matchedAttributes++;
            }
        }

        if (!isBlank(lost.getColor()) && !isBlank(found.getColor())) {
            comparableAttributes++;
            if (lost.getColor().trim().equalsIgnoreCase(found.getColor().trim())) {
                matchedAttributes++;
            }
        }

        if (comparableAttributes == 0) {
            return 50.0;
        }

        return (matchedAttributes / (double) comparableAttributes) * 100.0;
    }

    private String classifyConfidence(double totalScore) {
        if (totalScore >= HIGH_CONFIDENCE_THRESHOLD) return "HIGH";
        if (totalScore >= MEDIUM_CONFIDENCE_THRESHOLD) return "MEDIUM";
        return "LOW";
    }

    private String buildReason(double categoryScore, double descriptionScore, double locationScore,
                               double dateScore, double attributeScore, ItemReport lost) {
        StringBuilder sb = new StringBuilder();

        if (categoryScore >= 100.0) {
            sb.append("Same category (").append(lost.getCategory()).append("). ");
        }
        if (descriptionScore >= 40.0) {
            sb.append("Descriptions share similar keywords. ");
        }
        if (locationScore >= 75.0) {
            sb.append("Reported at the same or a very close location. ");
        } else if (locationScore > 0.0) {
            sb.append("Locations show some overlap. ");
        }
        if (dateScore >= 90.0) {
            sb.append("Reported on the same day or one day apart. ");
        } else if (dateScore >= 50.0) {
            sb.append("Reported within a reasonably close time frame. ");
        }
        if (attributeScore >= 100.0) {
            sb.append("Brand/color details match. ");
        }

        if (sb.length() == 0) {
            sb.append("Weak overall similarity across the matching criteria.");
        }

        return sb.toString().trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private LocalDate parseDate(String dateStr) {
        if (isBlank(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
