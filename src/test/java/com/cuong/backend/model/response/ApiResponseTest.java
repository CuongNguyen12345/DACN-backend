package com.cuong.backend.model.response;

import com.cuong.backend.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    void successCreatesDefaultSuccessResponseWithResult() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertEquals(1000, response.getCode());
        assertTrue(response.isSuccess());
        assertEquals("ok", response.getResult());
    }

    @Test
    void errorCreatesFailureResponseFromErrorCode() {
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.INVALID_TOKEN);

        assertEquals(ErrorCode.INVALID_TOKEN.getCode(), response.getCode());
        assertFalse(response.isSuccess());
        assertEquals(ErrorCode.INVALID_TOKEN.getMessage(), response.getMessage());
    }
}
