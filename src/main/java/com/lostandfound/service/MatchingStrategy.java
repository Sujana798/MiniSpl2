package com.lostandfound.service;

import com.lostandfound.model.ItemReport;

public interface MatchingStrategy {

    MatchScoreResult calculateScore(ItemReport lost, ItemReport found);
}
