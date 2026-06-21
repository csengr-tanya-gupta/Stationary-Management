package com.stationery.inventory.service;

import com.stationery.inventory.model.AuditRecord;
import com.stationery.inventory.repository.AuditRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditRecordRepository auditRecordRepository;

    public AuditService(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    @Transactional
    public void record(String username, String action, String entityType, String entityId, String details) {
        auditRecordRepository.save(new AuditRecord(username, action, entityType, entityId, details));
    }
}
