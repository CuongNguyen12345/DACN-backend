package com.cuong.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(
        name = "coin_transactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "source_type", "source_id"})
)
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoinTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "user_id", nullable = false)
    long userId;

    @Column(nullable = false)
    int amount;

    @Column(name = "source_type", nullable = false)
    String sourceType;

    @Column(name = "source_id", nullable = false)
    String sourceId;

    @Column(length = 500)
    String description;

    @Column(name = "created_at")
    @CreatedDate
    Date createdAt;
}
