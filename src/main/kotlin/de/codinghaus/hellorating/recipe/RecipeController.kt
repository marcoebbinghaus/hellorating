package de.codinghaus.hellorating.recipe

import de.codinghaus.hellorating.recipe.model.Recipe
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @PatchMapping("/{recipeId}")
    fun patchRecipe(@PathVariable recipeId:String, @RequestBody fields: Map<String, Any>): ResponseEntity<Recipe> {
        return ResponseEntity(recipeService.readRecipe(Integer.parseInt(recipeId)), HttpStatus.OK)
    }

}