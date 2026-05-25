package com.company.runcoach.integrations.strava.repo;

import com.company.runcoach.integrations.strava.domain.StravaConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StravaConnectionRepository extends JpaRepository<StravaConnection, UUID> {
    Optional<StravaConnection> findFirstByUser_IdAndDisconnectedAtIsNull(UUID userId);
}
