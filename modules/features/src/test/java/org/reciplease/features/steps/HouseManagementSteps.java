package org.reciplease.features.steps;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.features.support.ScenarioState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Drives the house-settings flows (member role changes, invite generation/listing/deletion)
 * end to end through MockMvc, against the real {@code HouseRepository}/{@code InviteRepository}
 * backed by embedded Mongo, reusing {@code HouseInviteSteps}'s house/invite seeding helpers.
 */
public class HouseManagementSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScenarioState state;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @When("{string} sets the role of {string} to {string} in the house")
    public void setsTheRoleOfInTheHouse(final String actorId, final String targetUserId, final String role)
            throws Exception {
        state.setLastResult(
                mockMvc.perform(houseScopedRequest(patch("/api/houses/members/{userId}", targetUserId), actorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"role": "%s"}""".formatted(role))));
    }

    @When("{string} generates an invite with role {string} in the house")
    public void generatesAnInviteWithRoleInTheHouse(final String actorId, final String role) throws Exception {
        final var result = mockMvc.perform(houseScopedRequest(post("/api/houses/invites"), actorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"role": "%s"}""".formatted(role)));
        state.setLastResult(result);

        final var responseBody = result.andReturn().getResponse().getContentAsString();
        if (!responseBody.isBlank()) {
            final var json = objectMapper.readTree(responseBody);
            state.setGeneratedInvite(json.get("id").asText(), json.get("code").asText());
        }
    }

    @When("{string} accepts the generated invite")
    public void acceptsTheGeneratedInvite(final String userId) throws Exception {
        state.setLastResult(mockMvc.perform(post("/api/invites/{code}/accept", state.generatedInviteCode())
                .with(jwt().jwt(builder -> builder.subject(userId)))));
    }

    @And("{string} deletes the generated invite in the house")
    public void deletesTheGeneratedInviteInTheHouse(final String actorId) throws Exception {
        state.setLastResult(mockMvc.perform(
                houseScopedRequest(delete("/api/houses/invites/{inviteId}", state.generatedInviteId()), actorId)));
    }

    @When("{string} lists pending invites in the house")
    public void listsPendingInvitesInTheHouse(final String actorId) throws Exception {
        state.setLastResult(mockMvc.perform(houseScopedRequest(get("/api/houses/invites"), actorId)));
    }

    @Then("the pending invites list does not contain code {string}")
    public void thePendingInvitesListDoesNotContainCode(final String code) throws Exception {
        state.lastResult()
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(not(containsString(code))));
    }

    private MockHttpServletRequestBuilder houseScopedRequest(
            final MockHttpServletRequestBuilder builder, final String userId) {
        return builder.with(csrf())
                .with(authentication(
                        new TestingAuthenticationToken(userId, "n/a", new SimpleGrantedAuthority("ROLE_RECIPLEASE"))))
                .header(HouseAccess.HOUSE_HEADER, state.houseId("Test House"));
    }
}
