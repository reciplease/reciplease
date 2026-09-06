package org.reciplease.controller;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.reciplease.dto.PublicRecipeDto;
import org.reciplease.dto.RecipeDto;
import org.reciplease.dto.UserSummaryDto;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
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
        final var soupDto = RecipeDto.from(soup, null, null);

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(soup.id(), OWNER_USER_ID)).thenReturn(Optional.of(soup));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(soupDto), true));
    }

    @Test
    @DisplayName("get all recipes")
    void allRecipes() throws Exception {
        final var recipes = List.of(getToast(), getSoup());
        final var recipeDtoList =
                recipes.stream().map(r -> RecipeDto.from(r, null, null)).collect(toList());

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
        final var savedSoupDto = RecipeDto.from(savedSoup, null, null);

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.create(OWNER_USER_ID, newSoupDto.toEntity())).thenReturn(savedSoup);

        final var json = mapper.writeValueAsString(newSoupDto);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(savedSoupDto), true));
    }

    @Test
    @DisplayName("create new recipe rejects an invalid sourceUrl")
    void createRecipeRejectsInvalidSourceUrl() throws Exception {
        final var invalidDto = PublicRecipeDto.builder()
                .name("soup")
                .sourceUrl("not-a-valid-url")
                .build();

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create new recipe rejects a name longer than 200 characters")
    void createRecipeRejectsOverlongName() throws Exception {
        final var invalidDto = PublicRecipeDto.builder().name("a".repeat(201)).build();

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
        final var updateDto = PublicRecipeDto.builder()
                .recipeId(recipe.id())
                .name("tomato soup")
                .build();

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));
        when(recipeService.update(recipe.id(), updateDto.toEntity())).thenReturn(updated);

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(RecipeDto.from(updated, null, null)), true));
    }

    @Test
    @DisplayName("update recipe is not found for a non-owner")
    void updateRecipeForbiddenForNonOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .createdBy("someone-else")
                .build();
        final var updateDto = PublicRecipeDto.builder()
                .recipeId(recipe.id())
                .name("tomato soup")
                .build();

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
        final var updateDto =
                PublicRecipeDto.builder().recipeId(id).name("tomato soup").build();

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
        final var expectedDto = RecipeDto.from(
                soup,
                UserSummaryDto.builder().userId(OWNER_USER_ID).handle("alice").build(),
                UserSummaryDto.builder().userId("user-2").handle("bob").build());

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(soup.id(), OWNER_USER_ID)).thenReturn(Optional.of(soup));
        when(userRepository.findById(OWNER_USER_ID)).thenReturn(Optional.of(ownerUser));
        when(userRepository.findById("user-2")).thenReturn(Optional.of(updatedBy));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedDto), true));
    }

    @Test
    @DisplayName("get recipe by ID omits owner info for a caller who is not the owner")
    void recipeOmitsOwnerInfoForNonOwner() throws Exception {
        final var soup = getSoup().toBuilder()
                .createdBy("someone-else")
                .updatedBy("user-2")
                .build();
        final var expectedDto = RecipeDto.from(soup);

        when(houseAccess.currentUserId()).thenReturn(OWNER_USER_ID);
        when(recipeService.findVisibleById(soup.id(), OWNER_USER_ID)).thenReturn(Optional.of(soup));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedDto), true));

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

    private PublicRecipeDto getNewSoupDto() {
        return PublicRecipeDto.builder().name("soup").build();
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
