package com.cuong.backend.model.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTeacherRequest {
    String name;
    String email;
    String password;
    /** Tên môn học FE (VD: "Toán", "Vật Lý", "Hóa Học", "Tiếng Anh") */
    String subject;
    /** Danh sách lớp được phân công (VD: ["Lớp 10", "Lớp 12"]) */
    List<String> grades;
}
