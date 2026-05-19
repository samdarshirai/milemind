package com.company.runcoach.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ArchitectureSkeletonTest {

    @Test
    void majorPackageSkeletonsArePresent() throws ClassNotFoundException {
        assertNotNull(Class.forName("com.company.runcoach.identity.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.profile.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.goals.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.planning.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.execution.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.adaptation.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.integrations.strava.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.ai.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.insights.package-info"));
    }

    @Test
    void recommendedSubpackagesArePresent() throws ClassNotFoundException {
        assertNotNull(Class.forName("com.company.runcoach.identity.api.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.identity.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.identity.domain.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.identity.repo.package-info"));

        assertNotNull(Class.forName("com.company.runcoach.profile.api.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.profile.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.profile.domain.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.profile.repo.package-info"));

        assertNotNull(Class.forName("com.company.runcoach.goals.api.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.goals.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.goals.domain.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.goals.repo.package-info"));

        assertNotNull(Class.forName("com.company.runcoach.planning.api.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.planning.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.planning.domain.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.planning.engine.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.planning.repo.package-info"));

        assertNotNull(Class.forName("com.company.runcoach.execution.api.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.execution.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.execution.domain.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.execution.repo.package-info"));

        assertNotNull(Class.forName("com.company.runcoach.adaptation.api.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.adaptation.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.adaptation.domain.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.adaptation.engine.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.adaptation.repo.package-info"));

        assertNotNull(Class.forName("com.company.runcoach.ai.api.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.ai.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.ai.prompt.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.ai.validation.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.ai.client.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.ai.repo.package-info"));

        assertNotNull(Class.forName("com.company.runcoach.insights.api.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.insights.service.package-info"));
        assertNotNull(Class.forName("com.company.runcoach.insights.query.package-info"));
    }
}
