package com.cuong.backend.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAccountDTO {
    long id;
    String userName;
    String email;
    String role;
    /** Giáo viên: tên môn (schoolName). Học viên: lớp (grade). */
    String unit;
    Date createdDate;
}
