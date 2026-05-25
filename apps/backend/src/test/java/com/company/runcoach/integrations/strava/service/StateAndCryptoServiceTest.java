package com.company.runcoach.integrations.strava.service;

import com.company.runcoach.integrations.strava.config.StravaProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StateAndCryptoServiceTest {

    @Test
    void stateHashIsDeterministicAndNonPlain() {
        StateHasher hasher = new StateHasher();
        String hash1 = hasher.hash("abc-state");
        String hash2 = hasher.hash("abc-state");

        assertEquals(hash1, hash2);
        assertNotEquals("abc-state", hash1);
    }

    @Test
    void encryptDecryptRoundTrip() {
        TokenCryptoService cryptoService = new TokenCryptoService(new StravaProperties("c", "s", "r", "a", "t", "d", Duration.ofMinutes(10), "auto", "read", Duration.ofMinutes(2), "state-crypto-test-secret"));
        String encrypted = cryptoService.encrypt("token-value");

        assertNotEquals("token-value", encrypted);
        assertEquals("token-value", cryptoService.decrypt(encrypted));
    }
}
