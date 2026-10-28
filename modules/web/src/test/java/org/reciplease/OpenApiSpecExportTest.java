package org.reciplease;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.ErrorResponseOperationCustomizer;
import org.reciplease.configuration.ErrorResponseSchemaRegistrar;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.PasskeyConfig;
import org.reciplease.configuration.ReciplaseJwtService;
import org.reciplease.configuration.SwaggerConfig;
import org.reciplease.configuration.WithMockRecipleaseUser;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.InviteRepository;
import org.reciplease.repository.UserIdentityRepository;
import org.reciplease.repository.UserRepository;
import org.reciplease.repository.WebAuthnChallengeLedger;
import org.reciplease.service.ApiKeyService;
import org.reciplease.service.FoodSearchService;
import org.reciplease.service.GoogleHealthAdapter;
import org.reciplease.service.InviteService;
import org.reciplease.service.PantryService;
import org.reciplease.service.PendingPantryService;
import org.reciplease.service.PlannedMealService;
import org.reciplease.service.RecipeService;
import org.reciplease.service.RefreshTokenService;
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
    SwaggerConfig.class,
    ErrorResponseOperationCustomizer.class,
    ErrorResponseSchemaRegistrar.class,
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
    private PantryService pantryService;

    @MockitoBean
    private PendingPantryService pendingPantryService;

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
    // GoogleHealthAdapter itself implements FoodConsumptionLoggerPort, so this single mock
    // satisfies both the concrete-typed and port-typed dependencies GoogleHealthController
    // needs — mirroring production, where one bean fulfills both roles. A separate
    // FoodConsumptionLoggerPort mock would create a second, ambiguous candidate bean.
    @MockitoBean
    private GoogleHealthAdapter googleHealthAdapter;

    @MockitoBean
    private FoodSearchService foodSearchService;

    @MockitoBean
    private ApiKeyService apiKeyService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Test
    void recipeMethodIsAUnionOfListAndTable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.components.schemas.RecipeMethod.oneOf[*].$ref",
                        containsInAnyOrder("#/components/schemas/ListMethod", "#/components/schemas/TableMethod")))
                .andExpect(jsonPath("$.components.schemas.ListMethod.required", contains("steps")))
                .andExpect(jsonPath("$.components.schemas.ListMethod.properties", not(hasKey("cells"))))
                .andExpect(jsonPath("$.components.schemas.TableMethod.required", contains("cells")))
                .andExpect(jsonPath("$.components.schemas.TableMethod.properties", not(hasKey("steps"))))
                .andExpect(jsonPath("$.components.schemas.RecipeRequest.required", hasItem("method")))
                .andExpect(jsonPath("$.components.schemas.RecipeRequest.properties", not(hasKey("steps"))))
                .andExpect(jsonPath("$.components.schemas.Recipe.required", hasItem("method")));
    }

    @Test
    void recipeSchemaUsesOwnershipObject() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.Recipe").exists())
                .andExpect(jsonPath("$.components.schemas.Recipe.properties.ownership.$ref")
                        .value("#/components/schemas/Ownership"))
                .andExpect(jsonPath("$.components.schemas.Recipe.properties.ownership.type")
                        .doesNotExist())
                .andExpect(jsonPath("$.components.schemas.Recipe.required", not(hasItem("ownership"))))
                .andExpect(
                        jsonPath("$.components.schemas.Recipe.properties.owned").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.Recipe.discriminator").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.PublicRecipe").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.OwnedRecipe").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.RecipeDto").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.Ownership.properties.createdBy")
                        .exists())
                .andExpect(jsonPath("$.components.schemas.Ownership.properties.updatedBy")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/recipes'].get.responses['200'].content['*/*'].schema.type")
                        .value("array"))
                .andExpect(jsonPath("$.paths['/api/recipes'].get.responses['200'].content['*/*'].schema.items.$ref")
                        .value("#/components/schemas/Recipe"))
                .andExpect(jsonPath("$.paths['/api/recipes/{uuid}'].get.responses['200'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/Recipe"))
                .andExpect(jsonPath("$.components.schemas.PlannedMeal.properties.recipe.$ref")
                        .value("#/components/schemas/Recipe"));
    }

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
