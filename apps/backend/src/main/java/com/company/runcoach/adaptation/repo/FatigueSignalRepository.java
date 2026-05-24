package com.company.runcoach.adaptation.repo;

import com.company.runcoach.adaptation.domain.FatigueSignal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface FatigueSignalRepository extends JpaRepository<FatigueSignal, UUID> {
    Optional<FatigueSignal> findByUser_IdAndSignalDateAndSource(UUID userId, LocalDate signalDate, String source);
    Optional<FatigueSignal> findFirstByUser_IdAndSignalDateOrderByCreatedAtDesc(UUID userId, LocalDate signalDate);
    Optional<FatigueSignal> findFirstByUser_IdAndSignalDateLessThanEqualOrderBySignalDateDescCreatedAtDesc(UUID userId, LocalDate signalDate);
    boolean existsByUser_IdAndSignalDate(UUID userId, LocalDate signalDate);
}
