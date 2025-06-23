package com.ntj.repository.audit;

import com.ntj.model.audit.AuditTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTaskRepository extends JpaRepository<AuditTask, Long> {

}
