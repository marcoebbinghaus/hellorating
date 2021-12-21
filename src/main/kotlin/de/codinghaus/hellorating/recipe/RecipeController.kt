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
        return ResponseEntity(recipeService.readRecipeById(Integer.parseInt(recipeId)), HttpStatus.OK)
    }

    @PatchMapping("/{recipeId}")
    fun patchRecipe(@PathVariable recipeId:Int, @RequestBody fields: Map<String, Any>): ResponseEntity<Recipe> {
        val rating = fields["rating"] as Int?
        if (rating != null) {
            val updatedRecipe = recipeService.updateRecipeRating(recipeId, rating)
            return ResponseEntity(updatedRecipe, HttpStatus.OK)
        }
        val notes = fields["notes"] as String?
        if (notes != null) {
            val updatedRecipe = recipeService.updateRecipeNotes(recipeId, notes)
            return ResponseEntity(updatedRecipe, HttpStatus.OK)
        }
        return ResponseEntity(recipeService.readRecipeById(recipeId), HttpStatus.OK)
    }

    @PostMapping
    fun createRecipe(@RequestBody fields: Map<String, Any>): ResponseEntity<Recipe> {
        val name = fields["name"] as String?
        val rating = fields["rating"] as Int?
        if (name == null) {
            throw IllegalArgumentException("Property 'name' must be given when posting recipes.")
        }
        if (rating == null) {
            throw IllegalArgumentException("Property 'rating' must be given when posting recipes.")
        }
        val updatedRecipe = recipeService.createRecipe(name = name, rating = rating)
        return ResponseEntity(updatedRecipe, HttpStatus.OK)
    }

}