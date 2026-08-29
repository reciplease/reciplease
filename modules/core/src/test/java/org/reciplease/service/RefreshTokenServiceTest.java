package org.reciplease.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.RefreshTokenRecord;
import org.reciplease.repository.RefreshTokenRepository;

class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration TTL = Duration.ofDays(30);

    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    private final RefreshTokenGenerator generator = mock(RefreshTokenGenerator.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, generator, clock, TTL);
        // Persist whatever's handed to save(), just stamping on an id — mirrors what a real repo does.
        when(repository.save(any())).thenAnswer(invocation -> {
            final RefreshTokenRecord record = invocation.getArgument(0);
            return new RefreshTokenRecord(
                    "generated-id",
                    record.userId(),
                    record.familyId(),
                    record.tokenPrefix(),
                    record.tokenHash(),
                    record.issuedAt(),
                    record.expiresAt(),
                    record.usedAt(),
                    record.revokedAt());
        });
    }

    @Test
    void issuePersistsARecordWithANewFamilyAndTheConfiguredTtl() {
        when(generator.generate()).thenReturn("raw-token-abcdefghijklmnop");

        final var issued = service.issue("user-1");

        assertThat(issued.rawToken()).isEqualTo("raw-token-abcdefghijklmnop");
        assertThat(issued.expiresAt()).isEqualTo(NOW.plus(TTL));

        final var captor = org.mockito.ArgumentCaptor.forClass(RefreshTokenRecord.class);
        verify(repository).save(captor.capture());
        final var saved = captor.getValue();
        assertThat(saved.userId()).isEqualTo("user-1");
        assertThat(saved.familyId()).isNotNull();
        assertThat(saved.tokenPrefix()).isEqualTo("raw-token-ab");
        assertThat(saved.tokenHash()).isNotNull().isNotEqualTo("raw-token-abcdefghijklmnop");
        assertThat(saved.issuedAt()).isEqualTo(NOW);
        assertThat(saved.expiresAt()).isEqualTo(NOW.plus(TTL));
        assertThat(saved.usedAt()).isNull();
        assertThat(saved.revokedAt()).isNull();
    }

    @Test
    void rotateOnAValidUnusedTokenMarksItUsedAndIssuesAFreshOneInTheSameFamily() {
        when(generator.generate()).thenReturn("original-raw-token1", "rotated-raw-token12");
        final var issued = service.issue("user-1");
        final var savedRecord = savedRecordFor("original-raw-token1");
        when(repository.findByPrefix(savedRecord.tokenPrefix())).thenReturn(Optional.of(savedRecord));

        final var result = service.rotate(issued.rawToken());

        assertThat(result).isInstanceOf(RefreshTokenService.Rotated.class);
        final var rotated = (RefreshTokenService.Rotated) result;
        assertThat(rotated.userId()).isEqualTo("user-1");
        assertThat(rotated.rawToken()).isEqualTo("rotated-raw-token12");
        assertThat(rotated.expiresAt()).isEqualTo(NOW.plus(TTL));

        verify(repository).markUsed(savedRecord.id(), NOW);

        final var captor = org.mockito.ArgumentCaptor.forClass(RefreshTokenRecord.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).familyId()).isEqualTo(savedRecord.familyId());
    }

    @Test
    void rotateOnAnUnknownPrefixIsInvalid() {
        when(repository.findByPrefix(anyString())).thenReturn(Optional.empty());

        final var result = service.rotate("some-unknown-raw-token123");

        assertThat(result).isInstanceOf(RefreshTokenService.Invalid.class);
        verify(repository, never()).markUsed(anyString(), any());
    }

    @Test
    void rotateOnAnExpiredTokenIsInvalid() {
        when(generator.generate()).thenReturn("original-raw-token1");
        final var issued = service.issue("user-1");
        final var record = savedRecordFor("original-raw-token1");
        final var expired = new RefreshTokenRecord(
                record.id(),
                record.userId(),
                record.familyId(),
                record.tokenPrefix(),
                record.tokenHash(),
                record.issuedAt(),
                NOW.minusSeconds(1),
                record.usedAt(),
                record.revokedAt());
        when(repository.findByPrefix(record.tokenPrefix())).thenReturn(Optional.of(expired));

        final var result = service.rotate(issued.rawToken());

        assertThat(result).isInstanceOf(RefreshTokenService.Invalid.class);
    }

    @Test
    void rotateOnARevokedTokenIsInvalid() {
        when(generator.generate()).thenReturn("original-raw-token1");
        final var issued = service.issue("user-1");
        final var record = savedRecordFor("original-raw-token1");
        final var revoked = new RefreshTokenRecord(
                record.id(),
                record.userId(),
                record.familyId(),
                record.tokenPrefix(),
                record.tokenHash(),
                record.issuedAt(),
                record.expiresAt(),
                record.usedAt(),
                NOW);
        when(repository.findByPrefix(record.tokenPrefix())).thenReturn(Optional.of(revoked));

        final var result = service.rotate(issued.rawToken());

        assertThat(result).isInstanceOf(RefreshTokenService.Invalid.class);
    }

    @Test
    void rotateOnAnAlreadyUsedTokenDetectsReuseAndRevokesTheFamily() {
        when(generator.generate()).thenReturn("original-raw-token1");
        final var issued = service.issue("user-1");
        final var record = savedRecordFor("original-raw-token1");
        final var alreadyUsed = new RefreshTokenRecord(
                record.id(),
                record.userId(),
                record.familyId(),
                record.tokenPrefix(),
                record.tokenHash(),
                record.issuedAt(),
                record.expiresAt(),
                NOW.minusSeconds(10),
                record.revokedAt());
        when(repository.findByPrefix(record.tokenPrefix())).thenReturn(Optional.of(alreadyUsed));

        final var result = service.rotate(issued.rawToken());

        assertThat(result).isInstanceOf(RefreshTokenService.ReuseDetected.class);
        assertThat(((RefreshTokenService.ReuseDetected) result).userId()).isEqualTo("user-1");
        verify(repository).revokeFamily(record.familyId(), NOW);
        verify(repository, never()).markUsed(anyString(), any());
    }

    @Test
    void revokeByRawTokenHappyPathRevokesAllForThatUser() {
        when(generator.generate()).thenReturn("original-raw-token1");
        final var issued = service.issue("user-1");
        final var record = savedRecordFor("original-raw-token1");
        when(repository.findByPrefix(record.tokenPrefix())).thenReturn(Optional.of(record));

        final var result = service.revokeByRawToken(issued.rawToken());

        assertThat(result).isTrue();
        verify(repository).revokeAllForUser("user-1", NOW);
    }

    @Test
    void revokeByRawTokenReturnsFalseWhenNotFound() {
        when(repository.findByPrefix(anyString())).thenReturn(Optional.empty());

        final var result = service.revokeByRawToken("unknown-raw-token12345");

        assertThat(result).isFalse();
        verify(repository, never()).revokeAllForUser(anyString(), any());
    }

    @Test
    void revokeAllForUserDelegatesToTheRepository() {
        service.revokeAllForUser("user-1");

        verify(repository).revokeAllForUser("user-1", NOW);
    }

    private RefreshTokenRecord savedRecordFor(final String rawToken) {
        final var captor = org.mockito.ArgumentCaptor.forClass(RefreshTokenRecord.class);
        verify(repository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getAllValues().stream()
                .filter(r -> r.tokenPrefix().equals(rawToken.substring(0, 12)))
                .reduce((first, second) -> second)
                .orElseThrow();
    }
}
