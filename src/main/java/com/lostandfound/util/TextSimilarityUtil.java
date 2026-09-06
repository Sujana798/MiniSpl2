package com.lostandfound.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class TextSimilarityUtil {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "of", "in", "on", "at", "is", "was",
            "with", "for", "to", "it", "my", "near", "by", "this", "that"
    ));

    private TextSimilarityUtil() {
    }

    public static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

    /** Splits text into lowercase word tokens, dropping stopwords and very short tokens. */
    public static Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        if (text == null || text.trim().isEmpty()) {
            return tokens;
        }
        String[] words = normalize(text).split("[^a-z0-9]+");
        for (String word : words) {
            if (word.length() > 1 && !STOPWORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    /** Jaccard similarity (intersection / union) between two token sets. Returns 0.0-1.0. */
    public static double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}
