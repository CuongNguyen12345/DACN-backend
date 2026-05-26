package com.cuong.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionLabelUtilTest {

    @Test
    void labelForIndexUsesLettersForTheFirstSixOptions() {
        assertEquals("A", OptionLabelUtil.labelForIndex(0));
        assertEquals("F", OptionLabelUtil.labelForIndex(5));
    }

    @Test
    void labelForIndexUsesOneBasedNumbersAfterTheLetterRange() {
        assertEquals("7", OptionLabelUtil.labelForIndex(6));
    }
}
