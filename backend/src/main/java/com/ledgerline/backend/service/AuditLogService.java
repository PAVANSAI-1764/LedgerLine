package com.ledgerline.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ledgerline.backend.dto.audit.AuditLogResponse;
import com.ledgerline.backend.model.entity.AuditLog;
import com.ledgerline.backend.repository.AuditLogRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorUsername(),
                log.getAction(),
                log.getEntityName(),
                log.getEntityId(),
                log.getIpAddress(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}