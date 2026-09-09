package com.lostandfound.state;

public class MatchedState implements ReportState {

    @Override
    public String getName() { return "MATCHED"; }

    @Override
    public ReportState markMatched() { return this; }

    @Override
    public ReportState markClaimed() { return new ClaimedState(); }

    @Override
    public ReportState markReturned() {
        throw new IllegalStateException("Cannot return a report that hasn't been claimed.");
    }

    @Override
    public boolean canBeClaimed() { return true; }
}