package com.company.runcoach.planning.repo;

import com.company.runcoach.planning.domain.TrainingPlanWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrainingPlanWeekRepository extends JpaRepository<TrainingPlanWeek, UUID> {
    List<TrainingPlanWeek> findByTrainingPlan_IdOrderByWeekIndexAsc(UUID trainingPlanId);
}
