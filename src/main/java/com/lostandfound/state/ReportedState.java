package com.lostandfound.state;

public class ReportedState implements ReportState {

    @Override
    public String getName() { return "REPORTED"; }

    @Override
    public ReportState markMatched() { return new MatchedState(); }

    @Override
    public ReportState markClaimed() {
        throw new IllegalStateException("Cannot claim a report that has no match yet.");
    }

    @Override
    public ReportState markReturned() {
        throw new IllegalStateException("Cannot return a report that hasn't been claimed.");
    }

    @Override
    public boolean canBeClaimed() { return false; }
}