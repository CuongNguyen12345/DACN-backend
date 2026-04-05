package com.cuong.backend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định"),
    INVALID_KEY(1001, "Invalid message key"),
    EMAIL_EXISTED(1002, "Email đã đăng kí trước đó"),
    EMAIL_EXISTED_GOOGLE(1003, "Email đã đăng kí bằng google"),
    USER_NOT_FOUND(1004, "Người dùng không tồn tại"),
    INVALID_PASSWORD(1005, "Mật khẩu phải có ít nhất 8 ký tự"),
    INVALID_USERNAME(1006, "Tên đăng nhập phải có ít nhất 7 ký tự"),
    INVALID_EMAIL(1007, "Email không hợp lệ"),
    USER_EXISTED(1008, "Tên đăng nhập đã tồn tại"),
    WRONG_PASSWORD(1009, "Mật khẩu không chính xác"),
    INVALID_OTP(1010, "Mã OTP không chính xác"),
    INVALID_TOKEN(1011, "Token không hợp lệ hoặc đã hết hạn");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
