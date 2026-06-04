package com.cuong.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "shop_items")
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(nullable = false)
    String name;

    @Column(length = 500)
    String description;

    @Column(nullable = false)
    String type;

    @Column(nullable = false)
    int price;

    @Column(name = "asset_url", length = 600)
    String assetUrl;

    @Column(name = "accent_color")
    String accentColor;

    @Column(name = "is_active")
    boolean active = true;

    @Column(name = "created_at")
    @CreatedDate
    Date createdAt;
}
