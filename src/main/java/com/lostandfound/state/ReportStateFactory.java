package com.lostandfound.state;

public class ReportStateFactory {

    public static ReportState fromString(String status) {
        return switch (status) {
            case "REPORTED" -> new ReportedState();
            case "MATCHED" -> new MatchedState();
            case "CLAIMED" -> new ClaimedState();
            case "RETURNED" -> new ReturnedState();
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }
}