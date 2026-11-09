package com.ledgerline.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ledgerline.backend.dto.audit.AuditLogResponse;
import com.ledgerline.backend.service.AuditLogService;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs(pageable));
    }
}
