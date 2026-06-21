package com.stationery.request.service;

import com.stationery.request.model.AuditRecord;
import com.stationery.request.repository.AuditRecordRepository;
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
