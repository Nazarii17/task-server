package com.ntj.repository;

import com.ntj.domain.entity.AppConfigurationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppConfigurationSnapshotRepository extends JpaRepository<AppConfigurationSnapshot, UUID> {

    @Query(value = "SELECT * FROM app_configuration_snapshot " +
            "WHERE app_name = :appName " +
            "ORDER BY snapshot_date_time " +
            "DESC LIMIT 1", nativeQuery = true)
    Optional<AppConfigurationSnapshot> findLastAppConfigurationSnapshot(@Param("appName") String appName);

    @Query(value = "SELECT * FROM app_configuration_snapshot " +
            "WHERE app_name = :appName AND task_id < :currentTaskId " +
            "ORDER BY snapshot_date_time " +
            "DESC LIMIT 1", nativeQuery = true)
    Optional<AppConfigurationSnapshot> findOneOfLastExecutionAppConfigurationSnapshot(@Param("appName") String appName,
                                                                                      @Param("currentTaskId") Long currentTaskId);

}
