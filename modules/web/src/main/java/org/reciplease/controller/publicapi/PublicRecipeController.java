package org.reciplease.controller.publicapi;

import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.dto.RecipeDto;
import org.reciplease.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Anonymous recipe browsing — {@code GET /api/recipes} and {@code GET /api/recipes/{uuid}} with
 * no {@code X-RCPLS-House-Id} header at all. Split from {@link org.reciplease.controller.RecipeController}
 * (which handles the same two paths, but only when that header <em>is</em> present — see its
 * {@code headers} mapping condition) so neither controller needs to branch on whether the header
 * showed up: Spring's {@code HandlerMapping} picks the right one before either method runs. No
 * house context reaches this controller by construction, so the response never carries house or
 * owner info — no need to consult {@link HouseAccess} at all.
 */
@RestController
@RequestMapping(value = "api/recipes", headers = "!" + HouseAccess.HOUSE_HEADER)
@RequiredArgsConstructor
public class PublicRecipeController {

    private final RecipeService recipeService;

    @GetMapping("{uuid}")
    public ResponseEntity<RecipeDto> findById(@PathVariable final String uuid) {
        return ResponseEntity.of(recipeService.findVisibleById(uuid, null).map(RecipeDto::from));
    }

    @GetMapping
    public ResponseEntity<List<RecipeDto>> findAll() {
        final var recipes =
                recipeService.findVisibleTo(null).stream().map(RecipeDto::from).collect(toList());
        return ResponseEntity.status(HttpStatus.OK).body(recipes);
    }
}
