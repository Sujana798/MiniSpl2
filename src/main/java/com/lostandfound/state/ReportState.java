package com.lostandfound.state;

public interface ReportState {

    String getName();
    ReportState markMatched();
    ReportState markClaimed();
    ReportState markReturned();
    boolean canBeClaimed();
}