package com.cuong.backend.util;

public final class OptionLabelUtil {
    private static final String[] OPTION_LABELS = { "A", "B", "C", "D", "E", "F" };

    private OptionLabelUtil() {
    }

    public static String labelForIndex(int index) {
        return index < OPTION_LABELS.length ? OPTION_LABELS[index] : String.valueOf(index + 1);
    }
}
