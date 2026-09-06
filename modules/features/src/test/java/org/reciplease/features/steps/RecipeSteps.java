package org.reciplease.features.steps;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Instant;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.features.support.ScenarioState;
import org.reciplease.model.RecipeDocument;
import org.reciplease.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class RecipeSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private ScenarioState state;

    @Given("a public recipe {string}")
    public void aPublicRecipe(final String recipeName) {
        final var recipe = RecipeDocument.builder()
                .id(recipeName.toLowerCase())
                .name(recipeName)
                .isPublic(true)
                .createdBy("owner-1")
                .createdAt(Instant.now())
                .build();
        mongoTemplate.save(recipe);
        state.putRecipeId(recipeName, recipe.getId());
    }

    @When("{string} upvotes recipe {string} in the house")
    public void upvotesRecipe(final String userId, final String recipeName) throws Exception {
        state.setLastResult(mockMvc.perform(
                houseScopedRequest(post("/api/recipes/{uuid}/upvote", state.recipeId(recipeName)), userId)));
    }

    @When("{string} removes their upvote on recipe {string} in the house")
    public void removesUpvote(final String userId, final String recipeName) throws Exception {
        state.setLastResult(mockMvc.perform(
                houseScopedRequest(delete("/api/recipes/{uuid}/upvote", state.recipeId(recipeName)), userId)));
    }

    @When("{string} lists recipes in the house")
    public void listsRecipes(final String userId) throws Exception {
        state.setLastResult(mockMvc.perform(houseScopedRequest(get("/api/recipes"), userId)));
    }

    @Then("recipe {string} is upvoted by {string}")
    public void recipeIsUpvotedBy(final String recipeName, final String userId) {
        final var recipe = recipeRepository.findById(state.recipeId(recipeName)).orElseThrow();
        if (!recipe.upvotedBy().contains(userId)) {
            throw new AssertionError("Expected recipe to be upvoted by " + userId);
        }
    }

    @Then("recipe {string} is not upvoted by {string}")
    public void recipeIsNotUpvotedBy(final String recipeName, final String userId) {
        final var recipe = recipeRepository.findById(state.recipeId(recipeName)).orElseThrow();
        if (recipe.upvotedBy().contains(userId)) {
            throw new AssertionError("Expected recipe to not be upvoted by " + userId);
        }
    }

    @Then("the first recipe in the list is {string}")
    public void firstRecipeInTheListIs(final String recipeName) throws Exception {
        state.lastResult().andExpect(jsonPath("$[0].name", is(recipeName)));
    }

    private MockHttpServletRequestBuilder houseScopedRequest(
            final MockHttpServletRequestBuilder builder, final String userId) {
        return builder.with(csrf())
                .with(authentication(
                        new TestingAuthenticationToken(userId, "n/a", new SimpleGrantedAuthority("ROLE_RECIPLEASE"))))
                .header(HouseAccess.HOUSE_HEADER, state.houseId("Test House"));
    }
}
