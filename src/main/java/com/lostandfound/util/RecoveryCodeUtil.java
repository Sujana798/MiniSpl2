package com.lostandfound.util;

import java.security.SecureRandom;

public class RecoveryCodeUtil {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int SEGMENT_LENGTH = 4;
    private static final int SEGMENT_COUNT = 3;
    private static final SecureRandom random = new SecureRandom();

    public static String generateRecoveryCode() {
        StringBuilder code = new StringBuilder();

        for (int segment = 0; segment < SEGMENT_COUNT; segment++) {
            if (segment > 0) {
                code.append("-");
            }
            for (int i = 0; i < SEGMENT_LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                code.append(CHARACTERS.charAt(index));
            }
        }

        return code.toString();
    }
}