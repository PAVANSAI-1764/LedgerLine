package com.ledgerline.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ledgerline.backend.model.entity.LedgerEntry;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByTransactionId(Long transactionId);
    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}