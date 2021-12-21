package de.codinghaus.hellorating.recipe

import de.codinghaus.hellorating.recipe.model.Recipe
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@RestController
@RequestMapping("/recipes")
class RecipeController(val recipeService: RecipeService) {

    @GetMapping
    fun getAllRecipes(): Collection<Recipe> {
        return recipeService.readAllRecipes()
    }

    @GetMapping("/{recipeId}")
    fun getRecipe(@PathVariable recipeId: String): ResponseEntity<Recipe> {
        return ResponseEntity(recipeService.readRecipeById(Integer.parseInt(recipeId)), HttpStatus.OK)
    }

    @PatchMapping("/{recipeId}")
    fun patchRecipe(@PathVariable recipeId: Int, @RequestBody fields: Map<String, Any>): ResponseEntity<Recipe> {
        val recipe = recipeService.readRecipeById(recipeId)
        val rating = fields["rating"] as Int?
        if (rating != null) {
            recipe.rating = rating
        }
        val notes = fields["notes"] as String?
        if (notes != null) {
            recipe.notes = notes
        }
        val name = fields["name"] as String?
        if (name != null) {
            recipe.name = name
        }
        return ResponseEntity(recipeService.applyRecipeDataToFile(recipe), HttpStatus.OK)
    }

    @PutMapping("/{recipeId}/pics/catalog")
    fun handleCatalogFileUpload(@PathVariable recipeId: Int,
                         @RequestParam("file") file: MultipartFile, redirectAttributes: RedirectAttributes): ResponseEntity<Recipe> {
        val updatedRecipe = recipeService.writePictureDataToFile(file.bytes, "catalog.jpeg", recipeId)
        return ResponseEntity(updatedRecipe, HttpStatus.OK)
    }

    @PutMapping("/{recipeId}/pics/own")
    fun handleOwnFileUpload(@PathVariable recipeId: Int,
                         @RequestParam("file") file: MultipartFile, redirectAttributes: RedirectAttributes): ResponseEntity<Recipe> {
        val updatedRecipe = recipeService.writePictureDataToFile(file.bytes, "own.jpeg", recipeId)
        return ResponseEntity(updatedRecipe, HttpStatus.OK)
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