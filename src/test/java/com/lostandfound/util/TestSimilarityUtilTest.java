package com.lostandfound.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TextSimilarityUtilTest {

    @Test
    void tokenize_shouldRemoveStopwordsAndShortWords() {
        Set<String> tokens = TextSimilarityUtil.tokenize("the black wallet near a library");

        assertTrue(tokens.contains("black"));
        assertTrue(tokens.contains("wallet"));
        assertTrue(tokens.contains("library"));
        assertFalse(tokens.contains("the"));
        assertFalse(tokens.contains("near"));
        assertFalse(tokens.contains("a"));
    }

    @Test
    void tokenize_withNullOrEmptyText_shouldReturnEmptySet() {
        assertTrue(TextSimilarityUtil.tokenize(null).isEmpty());
        assertTrue(TextSimilarityUtil.tokenize("").isEmpty());
        assertTrue(TextSimilarityUtil.tokenize("   ").isEmpty());
    }

    @Test
    void jaccardSimilarity_identicalSets_shouldReturnOne() {
        Set<String> a = Set.of("black", "wallet", "leather");
        Set<String> b = Set.of("black", "wallet", "leather");

        assertEquals(1.0, TextSimilarityUtil.jaccardSimilarity(a, b));
    }

    @Test
    void jaccardSimilarity_completelyDifferentSets_shouldReturnZero() {
        Set<String> a = Set.of("black", "wallet");
        Set<String> b = Set.of("blue", "backpack");

        assertEquals(0.0, TextSimilarityUtil.jaccardSimilarity(a, b));
    }

    @Test
    void jaccardSimilarity_withEmptySet_shouldReturnZero() {
        Set<String> a = Set.of("black", "wallet");
        Set<String> b = Set.of();

        assertEquals(0.0, TextSimilarityUtil.jaccardSimilarity(a, b));
    }

    @Test
    void jaccardSimilarity_partialOverlap_shouldReturnCorrectRatio() {
        Set<String> a = Set.of("black", "wallet", "leather");
        Set<String> b = Set.of("black", "wallet", "small");
        assertEquals(0.5, TextSimilarityUtil.jaccardSimilarity(a, b));
    }

    @Test
    void normalize_shouldTrimAndLowercase() {
        assertEquals("library", TextSimilarityUtil.normalize("  Library  "));
        assertEquals("", TextSimilarityUtil.normalize(null));
    }
}