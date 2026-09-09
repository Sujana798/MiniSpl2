package com.lostandfound.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportStateTest {

    @Test
    void reportedState_canTransitionToMatched() {
        ReportState state = new ReportedState();
        ReportState next = state.markMatched();

        assertEquals("MATCHED", next.getName());
    }

    @Test
    void reportedState_cannotBeClaimedDirectly() {
        ReportState state = new ReportedState();

        assertThrows(IllegalStateException.class, state::markClaimed);
        assertFalse(state.canBeClaimed());
    }

    @Test
    void matchedState_canBeClaimed() {
        ReportState state = new MatchedState();

        assertTrue(state.canBeClaimed());
        ReportState next = state.markClaimed();
        assertEquals("CLAIMED", next.getName());
    }

    @Test
    void claimedState_canTransitionToReturned() {
        ReportState state = new ClaimedState();
        ReportState next = state.markReturned();

        assertEquals("RETURNED", next.getName());
    }

    @Test
    void returnedState_cannotTransitionFurther() {
        ReportState state = new ReturnedState();

        assertThrows(IllegalStateException.class, state::markMatched);
        assertThrows(IllegalStateException.class, state::markClaimed);
    }

    @Test
    void factory_shouldReturnCorrectStateFromString() {
        assertInstanceOf(ReportedState.class, ReportStateFactory.fromString("REPORTED"));
        assertInstanceOf(MatchedState.class, ReportStateFactory.fromString("MATCHED"));
        assertInstanceOf(ClaimedState.class, ReportStateFactory.fromString("CLAIMED"));
        assertInstanceOf(ReturnedState.class, ReportStateFactory.fromString("RETURNED"));
    }

    @Test
    void factory_shouldThrowOnUnknownStatus() {
        assertThrows(IllegalArgumentException.class, () -> ReportStateFactory.fromString("UNKNOWN"));
    }
}