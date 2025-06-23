package com.ntj.service;

import com.ntj.model.audit.AuditTask;
import com.ntj.repository.audit.AuditTaskRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class AuditTaskPersister {

    private final AuditTaskRepository auditTaskRepository;

    @Transactional("secondaryTransactionManager")
    public void saveAuditTask(final AuditTask auditTask) {
        try {
            auditTaskRepository.save(auditTask);
            log.info("AuditTask saved successfully for customTaskId: {}", auditTask.getCustomTaskId());
        } catch (Exception e) {
            log.error("Failed to save AuditTask for customTaskId: {}", auditTask.getCustomTaskId(), e);
            throw new RuntimeException("Error saving audit task", e);
        }
    }
}
