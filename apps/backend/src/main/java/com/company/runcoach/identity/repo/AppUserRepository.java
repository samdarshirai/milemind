package com.company.runcoach.identity.repo;

import com.company.runcoach.identity.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<AppUser> findByEmailIgnoreCase(String email);
}
