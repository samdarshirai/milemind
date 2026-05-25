package com.company.runcoach.integrations.strava.repo;

import com.company.runcoach.integrations.strava.domain.StravaOauthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StravaOauthSessionRepository extends JpaRepository<StravaOauthSession, UUID> {
    Optional<StravaOauthSession> findByStateHash(String stateHash);
}
