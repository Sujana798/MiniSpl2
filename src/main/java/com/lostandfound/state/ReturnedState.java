package com.lostandfound.state;

public class ReturnedState implements ReportState {

    @Override
    public String getName() { return "RETURNED"; }

    @Override
    public ReportState markMatched() {
        throw new IllegalStateException("Report is already closed (returned).");
    }

    @Override
    public ReportState markClaimed() {
        throw new IllegalStateException("Report is already closed (returned).");
    }

    @Override
    public ReportState markReturned() { return this; }

    @Override
    public boolean canBeClaimed() { return false; }
}