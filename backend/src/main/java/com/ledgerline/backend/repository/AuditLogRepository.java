package com.ledgerline.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ledgerline.backend.model.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorUsernameOrderByCreatedAtDesc(String actorUsername);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}