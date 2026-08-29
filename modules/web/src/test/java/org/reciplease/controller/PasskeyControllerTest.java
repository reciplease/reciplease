package org.reciplease.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.PasskeyConfig;
import org.reciplease.configuration.ReciplaseJwtService;
import org.reciplease.model.User;
import org.reciplease.repository.IdentityConflictException;
import org.reciplease.repository.UserRepository;
import org.reciplease.repository.WebAuthnChallengeLedger;
import org.reciplease.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialParameters;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.ResidentKeyRequirement;
import org.springframework.security.web.webauthn.api.UserVerificationRequirement;
import org.springframework.security.web.webauthn.management.RelyingPartyRegistrationRequest;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PasskeyController.class)
@Import({PasskeyConfig.class, ReciplaseJwtService.class, MethodSecurityTestSupport.class})
class PasskeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebAuthnRelyingPartyOperations relyingPartyOperations;

    @MockitoBean
    private WebAuthnChallengeLedger challengeLedger;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void stubRefreshTokenIssuance() {
        when(refreshTokenService.issue(any()))
                .thenReturn(new RefreshTokenService.IssuedRefreshToken(
                        "issued-raw-refresh-token", Instant.now().plusSeconds(3600)));
    }

    private static PublicKeyCredentialCreationOptions creationOptions(final String challenge) {
        return PublicKeyCredentialCreationOptions.builder()
                .rp(PublicKeyCredentialRpEntity.builder()
                        .id("localhost")
                        .name("Reciplease")
                        .build())
                .user(ImmutablePublicKeyCredentialUserEntity.builder()
                        .id(new Bytes("user-1".getBytes()))
                        .name("user-1")
                        .displayName("user-1")
                        .build())
                .challenge(Bytes.fromBase64(challenge))
                .pubKeyCredParams(List.of(PublicKeyCredentialParameters.ES256))
                .build();
    }

    // The controller serializes this with the library's own Jackson module, which is what
    // we're really asserting on below — content is JSON, not the library's internal type.
    private static String attestationCredentialJson() {
        return """
                {"id":"credential-1","type":"public-key","rawId":"Y3JlZGVudGlhbC0x",\
                "response":{"attestationObject":"YXR0ZXN0YXRpb24","clientDataJSON":"Y2xpZW50RGF0YQ",\
                "transports":["internal"]},"clientExtensionResults":{}}""";
    }

    private static String assertionCredentialJson() {
        return """
                {"id":"credential-1","type":"public-key","rawId":"Y3JlZGVudGlhbC0x",\
                "response":{"authenticatorData":"YXV0aERhdGE","clientDataJSON":"Y2xpZW50RGF0YQ",\
                "signature":"c2lnbmF0dXJl","userHandle":"dXNlci0x"},"clientExtensionResults":{}}""";
    }

    private static String registerFinishJson(final String challenge, final String label) {
        return """
                {"challenge":"%s","credential":%s,"label":%s}\
                """.formatted(challenge, attestationCredentialJson(), label == null ? "null" : "\"" + label + "\"");
    }

    private static String loginFinishJson(final String challenge) {
        return """
                {"challenge":"%s","credential":%s}\
                """.formatted(challenge, assertionCredentialJson());
    }

    private static ImmutableCredentialRecord credentialRecord(final String userId) {
        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(Bytes.fromBase64("Y3JlZGVudGlhbC0x"))
                .userEntityUserId(new Bytes(userId.getBytes()))
                .publicKey(new ImmutablePublicKeyCose(new byte[] {1, 2, 3}))
                .attestationObject(new Bytes(new byte[] {1, 2, 3}))
                .signatureCount(0)
                .transports(Set.of(AuthenticatorTransport.INTERNAL))
                .created(Instant.now())
                .lastUsed(Instant.now())
                .build();
    }

    @Test
    void signupOptionsIssuesAFreshChallengeForAFreshlyMintedUserId() throws Exception {
        when(relyingPartyOperations.createPublicKeyCredentialCreationOptions(any()))
                .thenReturn(creationOptions("Y2hhbGxlbmdlLTE"));

        mockMvc.perform(post("/api/passkey/signup/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").value("Y2hhbGxlbmdlLTE"));

        verify(challengeLedger).issue(org.mockito.ArgumentMatchers.eq("Y2hhbGxlbmdlLTE"), any());
    }

    @Test
    void signupFinishCreatesANewUserAndLinksTheCredential() throws Exception {
        when(challengeLedger.consumeForRegistration("Y2hhbGxlbmdlLTE")).thenReturn(Optional.of("new-user-id"));
        when(relyingPartyOperations.registerCredential(any())).thenReturn(credentialRecord("new-user-id"));

        mockMvc.perform(post("/api/passkey/signup/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerFinishJson("Y2hhbGxlbmdlLTE", "My device")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("new-user-id"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value("issued-raw-refresh-token"));

        verify(userRepository).save(new User("new-user-id", null));
        verify(userRepository).linkIdentity("new-user-id", "passkey", "Y3JlZGVudGlhbC0x", null);
        verify(refreshTokenService).issue("new-user-id");
    }

    @Test
    void signupFinishPassesAuthenticatorSelectionToRegisterCredential() throws Exception {
        when(challengeLedger.consumeForRegistration("Y2hhbGxlbmdlLTE")).thenReturn(Optional.of("new-user-id"));
        when(relyingPartyOperations.registerCredential(any())).thenReturn(credentialRecord("new-user-id"));

        mockMvc.perform(post("/api/passkey/signup/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerFinishJson("Y2hhbGxlbmdlLTE", null)))
                .andExpect(status().isOk());

        final var captor = ArgumentCaptor.forClass(RelyingPartyRegistrationRequest.class);
        verify(relyingPartyOperations).registerCredential(captor.capture());
        assertThat(captor.getValue().getCreationOptions().getAuthenticatorSelection())
                .isNotNull();
    }

    @Test
    void signupFinishRejectsAChallengeThatWasNeverIssuedOrAlreadyUsed() throws Exception {
        when(challengeLedger.consumeForRegistration(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/passkey/signup/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerFinishJson("Y2hhbGxlbmdlLTE", null)))
                .andExpect(status().isUnauthorized());

        verify(relyingPartyOperations, never()).registerCredential(any());
    }

    @Test
    void registerOptionsRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/passkey/register/options")).andExpect(status().isUnauthorized());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user-1")
    void registerFinishLinksThePasskeyToTheAuthenticatedUser() throws Exception {
        when(challengeLedger.consumeForRegistration("Y2hhbGxlbmdlLTE")).thenReturn(Optional.of("user-1"));
        when(relyingPartyOperations.registerCredential(any())).thenReturn(credentialRecord("user-1"));

        mockMvc.perform(post("/api/passkey/register/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerFinishJson("Y2hhbGxlbmdlLTE", "My device")))
                .andExpect(status().isOk());

        verify(userRepository).linkIdentity("user-1", "passkey", "Y3JlZGVudGlhbC0x", null);
        verify(userRepository, never()).save(any());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user-1")
    void registerFinishPassesAuthenticatorSelectionToRegisterCredential() throws Exception {
        // Webauthn4JRelyingPartyOperations.registerCredential unconditionally dereferences
        // getAuthenticatorSelection().getUserVerification() — omitting it causes an NPE.
        when(challengeLedger.consumeForRegistration("Y2hhbGxlbmdlLTE")).thenReturn(Optional.of("user-1"));
        when(relyingPartyOperations.registerCredential(any())).thenReturn(credentialRecord("user-1"));

        mockMvc.perform(post("/api/passkey/register/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerFinishJson("Y2hhbGxlbmdlLTE", null)))
                .andExpect(status().isOk());

        final var captor = ArgumentCaptor.forClass(RelyingPartyRegistrationRequest.class);
        verify(relyingPartyOperations).registerCredential(captor.capture());
        final var selection = captor.getValue().getCreationOptions().getAuthenticatorSelection();
        assertThat(selection).isNotNull();
        assertThat(selection.getUserVerification()).isEqualTo(UserVerificationRequirement.PREFERRED);
        assertThat(selection.getResidentKey()).isEqualTo(ResidentKeyRequirement.REQUIRED);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user-1")
    void registerFinishRejectsAChallengeIssuedToADifferentUser() throws Exception {
        // Consume succeeds but for a different userId — must not link to the caller anyway.
        when(challengeLedger.consumeForRegistration("Y2hhbGxlbmdlLTE")).thenReturn(Optional.of("someone-else"));

        mockMvc.perform(post("/api/passkey/register/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerFinishJson("Y2hhbGxlbmdlLTE", null)))
                .andExpect(status().isUnauthorized());

        verify(relyingPartyOperations, never()).registerCredential(any());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user-1")
    void registerFinishReturnsConflictWhenTheCredentialIsAlreadyLinkedToSomeoneElse() throws Exception {
        when(challengeLedger.consumeForRegistration("Y2hhbGxlbmdlLTE")).thenReturn(Optional.of("user-1"));
        when(relyingPartyOperations.registerCredential(any())).thenReturn(credentialRecord("user-1"));
        org.mockito.Mockito.doThrow(new IdentityConflictException("passkey", "Y3JlZGVudGlhbC0x"))
                .when(userRepository)
                .linkIdentity("user-1", "passkey", "Y3JlZGVudGlhbC0x", null);

        mockMvc.perform(post("/api/passkey/register/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerFinishJson("Y2hhbGxlbmdlLTE", null)))
                .andExpect(status().isConflict());
    }

    @Test
    void loginOptionsIssuesADiscoverableChallenge() throws Exception {
        when(relyingPartyOperations.createCredentialRequestOptions(any()))
                .thenReturn(org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions.builder()
                        .challenge(Bytes.fromBase64("Y2hhbGxlbmdlLTI"))
                        .rpId("localhost")
                        .build());

        mockMvc.perform(post("/api/passkey/login/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").value("Y2hhbGxlbmdlLTI"));

        verify(challengeLedger).issue("Y2hhbGxlbmdlLTI", null);
    }

    @Test
    void loginFinishAuthenticatesAndMintsAToken() throws Exception {
        when(challengeLedger.consumeForLogin("Y2hhbGxlbmdlLTI")).thenReturn(true);
        final PublicKeyCredentialUserEntity userEntity = ImmutablePublicKeyCredentialUserEntity.builder()
                .id(new Bytes("user-1".getBytes()))
                .name("user-1")
                .displayName("user-1")
                .build();
        when(relyingPartyOperations.authenticate(any())).thenReturn(userEntity);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(post("/api/passkey/login/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginFinishJson("Y2hhbGxlbmdlLTI")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.handle").value("my-handle"))
                .andExpect(jsonPath("$.refreshToken").value("issued-raw-refresh-token"));

        verify(refreshTokenService).issue("user-1");
    }

    @Test
    void loginFinishRejectsAnInvalidAssertion() throws Exception {
        when(challengeLedger.consumeForLogin("Y2hhbGxlbmdlLTI")).thenReturn(true);
        when(relyingPartyOperations.authenticate(any())).thenThrow(new RuntimeException("bad signature"));

        mockMvc.perform(post("/api/passkey/login/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginFinishJson("Y2hhbGxlbmdlLTI")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginFinishRejectsAReplayedChallenge() throws Exception {
        when(challengeLedger.consumeForLogin(any())).thenReturn(false);

        mockMvc.perform(post("/api/passkey/login/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginFinishJson("Y2hhbGxlbmdlLTI")))
                .andExpect(status().isUnauthorized());

        verify(relyingPartyOperations, never()).authenticate(any());
    }
}
