package com.company.runcoach.profile.repo;

import com.company.runcoach.profile.domain.RunnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RunnerProfileRepository extends JpaRepository<RunnerProfile, UUID> {
    Optional<RunnerProfile> findByUser_Id(UUID userId);
}
