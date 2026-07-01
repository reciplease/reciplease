package org.reciplease;

import org.junit.jupiter.api.Test;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.PasskeyConfig;
import org.reciplease.configuration.ReciplaseJwtService;
import org.reciplease.configuration.WithMockRecipleaseUser;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.InviteRepository;
import org.reciplease.repository.UserIdentityRepository;
import org.reciplease.repository.UserRepository;
import org.reciplease.repository.WebAuthnChallengeLedger;
import org.reciplease.service.InventoryService;
import org.reciplease.service.InviteService;
import org.reciplease.service.PlannedMealService;
import org.reciplease.service.RecipeService;
import org.reciplease.service.ShoppingListService;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Generates a static OpenAPI spec without booting the full app (no database): loads every
 * {@code @RestController} in a single Spring MVC slice with every port/adapter mocked, then
 * dumps springdoc's live spec endpoint to {@code target/openapi.json}. Lets the frontend (or
 * a developer) regenerate TS types from the spec without standing up Mongo or the dist module.
 */
@WebMvcTest
@WithMockRecipleaseUser
@Import({
        MethodSecurityTestSupport.class,
        PasskeyConfig.class,
        ReciplaseJwtService.class,
        // springdoc's auto-configuration isn't part of @WebMvcTest's curated slice
        // auto-config allow-list, so it has to be imported explicitly here.
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        MultipleOpenApiSupportConfiguration.class,
        SpringDocWebMvcConfiguration.class,
})
class OpenApiSpecExportTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;
    @MockitoBean(name = "houseAccess")
    private HouseAccess houseAccess;
    @MockitoBean
    private InventoryService inventoryService;
    @MockitoBean
    private HouseRepository houseRepository;
    @MockitoBean
    private InviteService inviteService;
    @MockitoBean
    private InviteRepository inviteRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private UserIdentityRepository userIdentityRepository;
    @MockitoBean
    private PlannedMealService plannedMealService;
    @MockitoBean
    private ShoppingListService shoppingListService;
    @MockitoBean
    private WebAuthnRelyingPartyOperations relyingPartyOperations;
    @MockitoBean
    private WebAuthnChallengeLedger challengeLedger;

    @Test
    void exportsTheLiveSpecToTargetOpenapiJson() throws Exception {
        final var response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final var outputPath = Path.of("target/openapi.json");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, response.getContentAsString());
    }
}
