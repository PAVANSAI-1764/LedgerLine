package com.ledgerline.backend.dto.audit;

import java.time.Instant;

public class AuditLogResponse {

    private Long id;
    private String actorUsername;
    private String action;
    private String entityName;
    private String entityId;
    private String ipAddress;
    private String details;
    private Instant createdAt;

    public AuditLogResponse() {}

    public AuditLogResponse(Long id, String actorUsername, String action, String entityName, String entityId, String ipAddress, String details, Instant createdAt) {
        this.id = id;
        this.actorUsername = actorUsername;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.ipAddress = ipAddress;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}