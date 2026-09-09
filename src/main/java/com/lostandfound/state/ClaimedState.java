package com.lostandfound.state;

public class ClaimedState implements ReportState {

    @Override
    public String getName() { return "CLAIMED"; }

    @Override
    public ReportState markMatched() {
        throw new IllegalStateException("Report is already claimed, cannot re-match.");
    }

    @Override
    public ReportState markClaimed() { return this; }

    @Override
    public ReportState markReturned() { return new ReturnedState(); }

    @Override
    public boolean canBeClaimed() { return false; }
}