package de.codinghaus.hellorating.recipe

import de.codinghaus.hellorating.recipe.model.Recipe
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recipes")
class RecipeController(val recipeService: RecipeService) {

    @GetMapping
    fun getAllRecipes(): Collection<Recipe> {
        return recipeService.readAllRecipes()
    }

    @GetMapping("/{recipeId}")
    fun getRecipe(@PathVariable recipeId:String): ResponseEntity<Recipe> {
        return ResponseEntity(recipeService.readRecipe(Integer.parseInt(recipeId)), HttpStatus.OK)
    }

}