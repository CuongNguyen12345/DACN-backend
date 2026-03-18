package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "username")
    String userName;

    String email;

    String password;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "login_by_google")
    int loginByGoogle = 0;

    @Column(name = "createddate")
    @CreatedDate
    Date createdDate;

    @Column(name = "modifieddate")
    @LastModifiedDate
    Date modifiedDate;
}
