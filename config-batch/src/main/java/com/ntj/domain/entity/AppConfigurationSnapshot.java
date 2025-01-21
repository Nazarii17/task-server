package com.ntj.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "app_configuration_snapshot")
@Entity
public class AppConfigurationSnapshot {

    @Id()
    private UUID id;
    @Column(name = "app_name", nullable = false, length = 100)
    private String appName;
    @Column(name = "resources_data", columnDefinition = "TEXT")
    private String resourcesData;
    @Column(name = "property_sources", columnDefinition = "TEXT")
    private String propertySources;
    @Column(name = "snapshot_date_time")
    private LocalDateTime snapshotDateTime;
    @Column(name = "task_id")
    private Long taskId;

}
