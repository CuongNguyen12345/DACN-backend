package com.cuong.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthHeaderUtilTest {

    @Test
    void stripBearerPrefixRemovesBearerTokenPrefix() {
        assertEquals("abc.def.ghi", AuthHeaderUtil.stripBearerPrefix("Bearer abc.def.ghi"));
    }

    @Test
    void stripBearerPrefixKeepsRawTokenAndNullValues() {
        assertEquals("raw-token", AuthHeaderUtil.stripBearerPrefix("raw-token"));
        assertNull(AuthHeaderUtil.stripBearerPrefix(null));
    }
}
