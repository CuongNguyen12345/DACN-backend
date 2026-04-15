package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity {

    public UserEntity() {
    }

    public UserEntity(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "username")
    String userName;

    String email;

    String password;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "createddate")
    @CreatedDate
    Date createdDate;

    @Column(name = "modifieddate")
    @LastModifiedDate
    Date modifiedDate;
}