package org.reciplease.features.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.features.support.ScenarioState;
import org.reciplease.model.HouseDocument;
import org.reciplease.model.InviteDocument;
import org.reciplease.repository.AllowlistRepository;
import org.reciplease.repository.HouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Drives the invite-acceptance and house-scoped-data flows end to end through MockMvc, against
 * the real (non-mocked) {@code HouseRepository}/{@code AllowlistRepository}/{@code InviteRepository}
 * backed by an embedded Mongo. Houses/invites are seeded directly via {@code MongoTemplate},
 * mirroring how they're created by hand in production (there's no creation API for either).
 */
public class HouseInviteSteps {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private AllowlistRepository allowlistRepository;
    @Autowired
    private ScenarioState state;

    @Given("a house {string} owned by {string}")
    public void aHouseOwnedBy(final String houseName, final String ownerId) {
        final var members = new HashMap<String, String>();
        members.put(ownerId, "OWNER");
        final var house = HouseDocument.builder()
                .name(houseName)
                .members(members)
                .createdAt(Instant.now())
                .build();
        final var saved = mongoTemplate.save(house);
        state.putHouseId(houseName, saved.getId());
    }

    @Given("an invite code {string} for the house with role {string}")
    public void anInviteCodeForTheHouse(final String code, final String role) {
        anInviteCodeForHouse(code, "Test House", role);
    }

    @Given("an invite code {string} for house {string} with role {string}")
    public void anInviteCodeForHouse(final String code, final String houseName, final String role) {
        final var invite = InviteDocument.builder()
                .code(code)
                .houseId(state.houseId(houseName))
                .role(role)
                .createdAt(Instant.now())
                .build();
        mongoTemplate.save(invite);
    }

    @When("{string} with email {string} accepts invite {string}")
    public void acceptsInvite(final String userId, final String email, final String code) throws Exception {
        state.setLastResult(mockMvc.perform(post("/api/invites/{code}/accept", code)
                .with(jwt().jwt(builder -> builder.subject(userId).claim("email", email)))));
    }

    @When("{string} creates an inventory item named {string} in the house")
    public void createsInventoryItem(final String userId, final String itemName) throws Exception {
        createsInventoryItemInHouse(userId, itemName, "Test House");
    }

    @And("{string} creates an inventory item named {string} in house {string}")
    public void createsInventoryItemInHouse(final String userId, final String itemName, final String houseName) throws Exception {
        final var body = """
                {"name": "%s", "measure": "ITEMS", "amount": 1, "expiration": "2030-01-01"}
                """.formatted(itemName);

        state.setLastResult(mockMvc.perform(houseScopedRequest(post("/api/inventory"), userId, houseName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)));
    }

    @When("{string} lists inventory in the house")
    public void listsInventory(final String userId) throws Exception {
        state.setLastResult(mockMvc.perform(houseScopedRequest(get("/api/inventory"), userId, "Test House")));
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(final int status) throws Exception {
        state.lastResult().andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(status));
    }

    @Then("{string} is on the allowlist")
    public void isOnTheAllowlist(final String email) {
        assertThat(allowlistRepository.contains(email)).isTrue();
    }

    @Then("{string} has role {string} in the house")
    public void hasRoleInTheHouse(final String userId, final String role) {
        final var houseId = state.houseId("Test House");
        final var actualRole = houseRepository.roleOf(houseId, userId);
        assertThat(actualRole).isPresent();
        assertThat(actualRole.get().name()).isEqualTo(role);
    }

    @Then("the inventory list contains {string}")
    public void theInventoryListContains(final String itemName) throws Exception {
        state.lastResult().andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString(itemName)));
    }

    @Then("the inventory list does not contain {string}")
    public void theInventoryListDoesNotContain(final String itemName) throws Exception {
        state.lastResult().andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(itemName))));
    }

    private MockHttpServletRequestBuilder houseScopedRequest(final MockHttpServletRequestBuilder builder, final String userId, final String houseName) {
        return builder
                .with(authentication(new TestingAuthenticationToken(userId, "n/a", new SimpleGrantedAuthority("ROLE_RECIPLEASE"))))
                .header(HouseAccess.HOUSE_HEADER, state.houseId(houseName));
    }
}
