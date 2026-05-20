package com.company.runcoach.goals.repo;

import com.company.runcoach.goals.domain.RaceGoal;
import com.company.runcoach.goals.domain.RaceGoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RaceGoalRepository extends JpaRepository<RaceGoal, UUID> {
    boolean existsByUser_IdAndStatus(UUID userId, RaceGoalStatus status);
    Optional<RaceGoal> findByUser_IdAndStatus(UUID userId, RaceGoalStatus status);
}
