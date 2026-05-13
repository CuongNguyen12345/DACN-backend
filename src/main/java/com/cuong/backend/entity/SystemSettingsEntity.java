package com.cuong.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "system_settings")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemSettingsEntity {

    @Id
    Long id;

    @Column(name = "prevent_tab_switch", nullable = false)
    boolean preventTabSwitch = true;

    @Column(name = "prevent_copy", nullable = false)
    boolean preventCopy = true;

    @Column(name = "show_result_immediately", nullable = false)
    boolean showResultImmediately = false;
}
