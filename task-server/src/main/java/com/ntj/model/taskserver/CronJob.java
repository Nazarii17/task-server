package com.ntj.model.taskserver;

import com.ntj.model.taskserver.dto.CronJobDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cron_job")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jobName;

    @Column(nullable = false)
    private String cronExpression; // e.g., "0 0/2 * * * ?"

    public CronJobDTO toDTO() {
        return new CronJobDTO(id, jobName, cronExpression);
    }
}
