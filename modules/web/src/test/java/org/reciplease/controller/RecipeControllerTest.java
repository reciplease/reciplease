package org.reciplease.controller;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.WithMockRecipleaseUser;
import org.reciplease.dto.ListMethodDto;
import org.reciplease.dto.RecipeDto;
import org.reciplease.dto.RecipeRequest;
import org.reciplease.dto.UserSummaryDto;
import org.reciplease.model.CellDuration;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.model.TableCell;
import org.reciplease.model.TableMethod;
import org.reciplease.model.User;
import org.reciplease.repository.UserRepository;
import org.reciplease.service.RecipeService;
import org.reciplease.service.request.AddIngredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Imports {@link org.reciplease.configuration.MethodSecurityTestSupport} to exercise the same
 * {@code @PreAuthorize} annotations the controller uses under real method security.
 */
@WebMvcTest(RecipeController.class)
@WithMockRecipleaseUser
@Import(MethodSecurityTestSupport.class)
class RecipeControllerTest {

    private static final String OWNER_USER_ID = "user-owner";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean(name = "houseAccess")
    private HouseAccess houseAccess;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("ID does not exist")
    void noRecipe() throws Exception {
        final var id = UUID.randomUUID().toString();
        when(recipeService.findVisibleById(id, null)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/recipes/{uuid}", id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get recipe by ID as the owner returns the owned view")
    void recipeAsOwner() throws Exception {
        final var soup = getSoup();
        final var soupDto = RecipeDto.ownerView(soup, null, null, OWNER_USER_ID);

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(soup.id(), OWNER_USER_ID)).thenReturn(Optional.of(soup));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(soupDto), true))
                .andExpect(jsonPath("$.ownership").exists())
                .andExpect(jsonPath("$.owned").doesNotExist());
    }

    @Test
    @DisplayName("get all recipes")
    void allRecipes() throws Exception {
        final var recipes = List.of(getToast(), getSoup());
        final var recipeDtoList = recipes.stream()
                .map(r -> RecipeDto.ownerView(r, null, null, OWNER_USER_ID))
                .collect(toList());

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleTo(OWNER_USER_ID)).thenReturn(recipes);

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(recipeDtoList), true));
    }

    @Test
    @DisplayName("create new recipe as an authenticated user")
    void createRecipe() throws Exception {
        final var newSoupDto = getNewSoupDto();
        final var savedSoup = getSavedSoup();
        final var savedSoupDto = RecipeDto.ownerView(savedSoup, null, null, OWNER_USER_ID);

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.create(OWNER_USER_ID, newSoupDto.toEntity())).thenReturn(savedSoup);

        final var json = mapper.writeValueAsString(newSoupDto);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(savedSoupDto), true))
                .andExpect(jsonPath("$.ownership").exists())
                .andExpect(jsonPath("$.owned").doesNotExist());
    }

    @Test
    @DisplayName("create new recipe accepts a body without the read-only and discriminator fields")
    void createRecipeWithWritableFieldsOnly() throws Exception {
        final var savedSoup = getSavedSoup();
        final var body = """
                {"name":"soup","method":{"steps":[]},"ingredients":[],
                 "prepTimeMinutes":5,"cookTimeMinutes":40,"servings":4}
                """;

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.create(eq(OWNER_USER_ID), any(Recipe.class))).thenReturn(savedSoup);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(recipeService)
                .create(
                        eq(OWNER_USER_ID),
                        argThat(recipe -> recipe.prepTimeMinutes() == 5
                                && recipe.cookTimeMinutes() == 40
                                && recipe.servings() == 4
                                && !recipe.isPublic()));
    }

    @Test
    @DisplayName("create new recipe ignores response-only fields sent by older clients")
    void createRecipeIgnoresResponseOnlyFields() throws Exception {
        final var body = """
                {"name":"soup","description":"","sourceUrl":"","isPublic":false,"method":{"steps":[]},"ingredients":[],
                 "owned":false,"upvoteCount":3,"upvotedByCurrentUser":true,"recipeId":"someone-elses-id"}
                """;

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.create(eq(OWNER_USER_ID), any(Recipe.class))).thenReturn(getSavedSoup());

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(recipeService).create(eq(OWNER_USER_ID), argThat(recipe -> recipe.id() == null));
    }

    @Test
    @DisplayName("create new recipe accepts a table method")
    void createTableRecipe() throws Exception {
        final var body = """
                {"name":"toast","ingredients":[],
                 "method":{"cells":[{"text":"toast","inputs":[{"text":"bread","inputs":[]}]}]}}
                """;

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.create(eq(OWNER_USER_ID), any(Recipe.class))).thenReturn(getSavedSoup());

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(recipeService)
                .create(
                        eq(OWNER_USER_ID),
                        argThat(recipe -> recipe.method()
                                .equals(new TableMethod(
                                        List.of(new TableCell("toast", List.of(TableCell.row("bread"))))))));
    }

    @Test
    @DisplayName("create new recipe keeps cell duration and handsOff, nested inputs included")
    void createTableRecipeWithDuration() throws Exception {
        final var body = """
                {"name":"roast","ingredients":[],
                 "method":{"cells":[{"text":"roast","handsOff":true,"duration":{"minMinutes":45,"maxMinutes":60},
                   "inputs":[{"text":"rest","inputs":[],"duration":{"minMinutes":10,"maxMinutes":10}}]}]}}
                """;

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.create(eq(OWNER_USER_ID), any(Recipe.class))).thenReturn(getSavedSoup());

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(recipeService)
                .create(
                        eq(OWNER_USER_ID),
                        argThat(recipe -> recipe.method()
                                .equals(new TableMethod(List.of(new TableCell(
                                        "roast",
                                        List.of(new TableCell("rest", List.of(), new CellDuration(10, 10), false)),
                                        new CellDuration(45, 60),
                                        true))))));
    }

    @Test
    @DisplayName("create new recipe rejects a cell duration whose max is below its min")
    void createTableRecipeRejectsInvalidRange() throws Exception {
        final var body = """
                {"name":"roast","ingredients":[],
                 "method":{"cells":[{"text":"roast","inputs":[],"duration":{"minMinutes":60,"maxMinutes":45}}]}}
                """;

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(recipeService, never()).create(any(), any());
    }

    @Test
    @DisplayName("create new recipe rejects a negative cell duration and a half-specified one")
    void createTableRecipeRejectsNegativeAndPartialDuration() throws Exception {
        final var negative = """
                {"name":"roast","ingredients":[],
                 "method":{"cells":[{"text":"roast","inputs":[],"duration":{"minMinutes":-1,"maxMinutes":5}}]}}
                """;
        final var partial = """
                {"name":"roast","ingredients":[],
                 "method":{"cells":[{"text":"roast","inputs":[],"duration":{"minMinutes":5}}]}}
                """;

        for (final var body : List.of(negative, partial)) {
            mockMvc.perform(post("/api/recipes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("update recipe round-trips a cell duration and handsOff into the response")
    void updateTableRecipeRoundTripsDuration() throws Exception {
        final var cell = new TableCell("bake", List.of(TableCell.row("dough")), new CellDuration(20, 25), true);
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("bread")
                .createdBy(OWNER_USER_ID)
                .build();
        final var updated =
                recipe.toBuilder().method(new TableMethod(List.of(cell))).build();
        final var body = """
                {"name":"bread","ingredients":[],
                 "method":{"cells":[{"text":"bake","handsOff":true,"duration":{"minMinutes":20,"maxMinutes":25},
                   "inputs":[{"text":"dough","inputs":[]}]}]}}
                """;

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));
        when(recipeService.update(eq(recipe.id()), any(Recipe.class))).thenReturn(updated);

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method.cells[0].duration.minMinutes").value(20))
                .andExpect(jsonPath("$.method.cells[0].duration.maxMinutes").value(25))
                .andExpect(jsonPath("$.method.cells[0].handsOff").value(true))
                .andExpect(jsonPath("$.method.cells[0].inputs[0].handsOff").value(false))
                .andExpect(jsonPath("$.method.cells[0].inputs[0].duration").doesNotExist());
    }

    @Test
    @DisplayName("update recipe rejects an invalid cell duration")
    void updateTableRecipeRejectsInvalidRange() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("bread")
                .createdBy(OWNER_USER_ID)
                .build();
        final var body = """
                {"name":"bread","ingredients":[],
                 "method":{"cells":[{"text":"bake","inputs":[],"duration":{"minMinutes":30,"maxMinutes":20}}]}}
                """;

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(recipeService, never()).update(any(), any());
    }

    @Test
    @DisplayName("get recipe returns cell duration and handsOff")
    void getTableRecipeWithDuration() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("bread")
                .createdBy(OWNER_USER_ID)
                .method(new TableMethod(List.of(new TableCell("bake", List.of(), new CellDuration(20, 20), true))))
                .build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(recipe.id(), OWNER_USER_ID)).thenReturn(Optional.of(recipe));

        mockMvc.perform(get("/api/recipes/{uuid}", recipe.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method.cells[0].duration.minMinutes").value(20))
                .andExpect(jsonPath("$.method.cells[0].duration.maxMinutes").value(20))
                .andExpect(jsonPath("$.method.cells[0].handsOff").value(true));
    }

    @Test
    @DisplayName("create new recipe rejects a method with both steps and cells")
    void createRecipeRejectsStepsAndCells() throws Exception {
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"soup\",\"ingredients\":[],\"method\":{\"steps\":[],\"cells\":[]}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create new recipe rejects a body without a method")
    void createRecipeRejectsMissingMethod() throws Exception {
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"soup\",\"ingredients\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create new recipe rejects the old top-level steps without a method")
    void createRecipeRejectsTopLevelSteps() throws Exception {
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"soup\",\"ingredients\":[],\"steps\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create new recipe rejects an empty method")
    void createRecipeRejectsEmptyMethod() throws Exception {
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"soup\",\"ingredients\":[],\"method\":{}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create new recipe rejects a body without ingredients")
    void createRecipeRejectsMissingIngredients() throws Exception {
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"soup\",\"method\":{\"steps\":[]}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update recipe rejects a body without a method or ingredients instead of clearing them")
    void updateRecipeRejectsMissingLists() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy(OWNER_USER_ID)
                .build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"tomato soup\"}"))
                .andExpect(status().isBadRequest());

        verify(recipeService, never()).update(any(), any());
    }

    @Test
    @DisplayName("create new recipe rejects an invalid sourceUrl")
    void createRecipeRejectsInvalidSourceUrl() throws Exception {
        final var invalidDto = soupRequest().sourceUrl("not-a-valid-url").build();

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create new recipe rejects a name longer than 200 characters")
    void createRecipeRejectsOverlongName() throws Exception {
        final var invalidDto = soupRequest().name("a".repeat(201)).build();

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("add ingredient as the owner")
    void addRecipeIngredientAsOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy(OWNER_USER_ID)
                .build();

        final var addIngredientRequest = new AddIngredient("tomato", "ITEMS", 10d);
        final var savedRecipeIngredient = new RecipeIngredient("tomato", "ITEMS", 10d);

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));
        when(recipeService.addIngredient(recipe.id(), addIngredientRequest)).thenReturn(Set.of(savedRecipeIngredient));

        final var data = "{\"name\": \"tomato\", \"measure\": \"ITEMS\", \"amount\": 10.0}";
        final var expectedJson = "[{\"name\": \"tomato\", \"measure\": \"item\", \"amount\": 10.0}]";

        mockMvc.perform(put("/api/recipes/{uuid}/ingredients", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    @DisplayName("add ingredient is not found for a non-owner")
    void addRecipeIngredientForbiddenForNonOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy("someone-else")
                .build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));

        final var data = "{\"name\": \"tomato\", \"measure\": \"ITEMS\", \"amount\": 10.0}";

        mockMvc.perform(put("/api/recipes/{uuid}/ingredients", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("update recipe when the current user is the owner")
    void updateRecipeAsOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy(OWNER_USER_ID)
                .build();
        final var updated = recipe.toBuilder().name("tomato soup").build();
        final var updateDto = soupRequest().name("tomato soup").build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));
        when(recipeService.update(recipe.id(), updateDto.toEntity())).thenReturn(updated);

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json(
                                mapper.writeValueAsString(RecipeDto.ownerView(updated, null, null, OWNER_USER_ID)),
                                true));
    }

    @Test
    @DisplayName("update recipe is not found for a non-owner")
    void updateRecipeForbiddenForNonOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy("someone-else")
                .build();
        final var updateDto = soupRequest().name("tomato soup").build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(recipeService, never()).update(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("update recipe returns 404 when it does not exist")
    void updateRecipeNotFound() throws Exception {
        final var id = UUID.randomUUID().toString();
        final var updateDto = soupRequest().name("tomato soup").build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/recipes/{uuid}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get recipe by ID includes createdBy and resolved handles for the owner")
    void recipeIncludesOwnerInfoForOwner() throws Exception {
        final var soup = getSoup().toBuilder()
                .createdBy(OWNER_USER_ID)
                .updatedBy("user-2")
                .build();
        final var ownerUser = new User(OWNER_USER_ID, "alice");
        final var updatedBy = new User("user-2", "bob");
        final var expectedDto = RecipeDto.ownerView(
                soup,
                UserSummaryDto.builder().userId(OWNER_USER_ID).handle("alice").build(),
                UserSummaryDto.builder().userId("user-2").handle("bob").build(),
                OWNER_USER_ID);

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(soup.id(), OWNER_USER_ID)).thenReturn(Optional.of(soup));
        when(userRepository.findById(OWNER_USER_ID)).thenReturn(Optional.of(ownerUser));
        when(userRepository.findById("user-2")).thenReturn(Optional.of(updatedBy));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedDto), true))
                .andExpect(jsonPath("$.ownership.createdBy.handle").value("alice"))
                .andExpect(jsonPath("$.owned").doesNotExist());
    }

    @Test
    @DisplayName("get recipe by ID omits owner info for a caller who is not the owner")
    void recipeOmitsOwnerInfoForNonOwner() throws Exception {
        final var soup = getSoup().toBuilder()
                .createdBy("someone-else")
                .updatedBy("user-2")
                .build();
        final var expectedDto = RecipeDto.publicView(soup, OWNER_USER_ID);

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(soup.id(), OWNER_USER_ID)).thenReturn(Optional.of(soup));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedDto), true))
                .andExpect(jsonPath("$.ownership").doesNotExist())
                .andExpect(jsonPath("$.owned").doesNotExist());

        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("delete recipe by ID as the owner")
    void deleteRecipeAsOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy(OWNER_USER_ID)
                .build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));

        mockMvc.perform(delete("/api/recipes/{uuid}", recipe.id())).andExpect(status().isNoContent());

        verify(recipeService).deleteById(recipe.id());
    }

    @Test
    @DisplayName("delete recipe by ID returns 404 for a non-owner")
    void deleteRecipeForbiddenForNonOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy("someone-else")
                .build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));

        mockMvc.perform(delete("/api/recipes/{uuid}", recipe.id())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("upvote a visible recipe")
    void upvoteRecipe() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .isPublic(true)
                .build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(recipe.id(), OWNER_USER_ID)).thenReturn(Optional.of(recipe));

        mockMvc.perform(post("/api/recipes/{uuid}/upvote", recipe.id())).andExpect(status().isNoContent());

        verify(recipeService).upvote(recipe.id(), OWNER_USER_ID);
    }

    @Test
    @DisplayName("upvote returns 404 when the recipe is not visible")
    void upvoteRecipeNotFound() throws Exception {
        final var id = UUID.randomUUID().toString();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(id, OWNER_USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/recipes/{uuid}/upvote", id)).andExpect(status().isNotFound());

        verify(recipeService, never()).upvote(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("remove upvote on a visible recipe")
    void removeUpvoteRecipe() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .isPublic(true)
                .build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(recipe.id(), OWNER_USER_ID)).thenReturn(Optional.of(recipe));

        mockMvc.perform(delete("/api/recipes/{uuid}/upvote", recipe.id())).andExpect(status().isNoContent());

        verify(recipeService).removeUpvote(recipe.id(), OWNER_USER_ID);
    }

    @Test
    @DisplayName("remove upvote returns 404 when the recipe is not visible")
    void removeUpvoteRecipeNotFound() throws Exception {
        final var id = UUID.randomUUID().toString();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(id, OWNER_USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/recipes/{uuid}/upvote", id)).andExpect(status().isNotFound());

        verify(recipeService, never())
                .removeUpvote(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private RecipeRequest getNewSoupDto() {
        return soupRequest().build();
    }

    private RecipeRequest.RecipeRequestBuilder soupRequest() {
        return RecipeRequest.builder()
                .name("soup")
                .method(new ListMethodDto(List.of()))
                .ingredients(Set.of());
    }

    private Recipe getSavedSoup() {
        return Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy(OWNER_USER_ID)
                .build();
    }

    private Recipe getSoup() {
        return Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy(OWNER_USER_ID)
                .build()
                .addIngredient("tomato", "ITEMS", 5d);
    }

    private Recipe getToast() {
        return Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("toast")
                .createdBy(OWNER_USER_ID)
                .build()
                .addIngredient("bread", "ITEMS", 1d);
    }
}
