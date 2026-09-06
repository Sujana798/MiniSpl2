package com.lostandfound.service;

import com.lostandfound.model.ItemReport;

/**
 * Strategy Pattern interface for pluggable match-scoring algorithms.
 * MatchService depends only on this interface, so a different scoring
 * approach (e.g. adding photo similarity later) can be introduced by adding
 * a new implementation - no changes required to MatchService itself.
 */
public interface MatchingStrategy {

    MatchScoreResult calculateScore(ItemReport lost, ItemReport found);
}
