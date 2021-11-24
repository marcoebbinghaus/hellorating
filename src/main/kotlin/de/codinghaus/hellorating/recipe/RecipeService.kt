package de.codinghaus.hellorating.recipe

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.codinghaus.hellorating.configuration.ConfigurationService
import de.codinghaus.hellorating.exception.model.EntityNotFoundException
import de.codinghaus.hellorating.recipe.model.JsonRecipeData
import de.codinghaus.hellorating.recipe.model.Recipe
import de.codinghaus.hellorating.recipe.model.isValidRecipe
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.stereotype.Component
import java.io.File

@Component
class RecipeService(val configurationService: ConfigurationService) {

    val recipeSubPathPattern = "/recipe%03d"

    fun readRecipe(recipeNo: Int): Recipe {
        val completePath =
            "${configurationService.recipeBasePath()}" + String.format(recipeSubPathPattern, recipeNo)
        return readRecipe(completePath)
    }

    private fun readRecipe(completePathToRecipeFolder: String): Recipe {
        val recipeFolder = File(completePathToRecipeFolder)
        var jsonRecipeData: JsonRecipeData? = null
        var recipeCatalogPicture: String? = null
        var recipeOwnPicture: String? = null
        recipeFolder.walk().maxDepth(1).forEach { file ->
            if (file.isFile) {
                when {
                    file.name.equals("recipe-data.json") -> jsonRecipeData = parseRecipeDataFromJson(file.readText())
                    file.name.equals("catalog.jpeg") -> recipeCatalogPicture =
                        Base64.encodeBase64String(file.readBytes())
                    file.name.equals("own.jpeg") -> recipeOwnPicture = Base64.encodeBase64String(file.readBytes())
                    else -> println("Skipped unknown file ${file.name} for recipe ${recipeFolder.name}!")
                }
            } else {
                if (file != recipeFolder) {
                    println("Skipped unknown sub folder ${file.name} for recipe ${recipeFolder.name}!")
                }
            }
        }
        val recipeFolderName = recipeFolder.name.substringAfterLast(File.separator)
        val recipeNo = Integer.parseInt(recipeFolderName.takeLast(3))
        if (jsonRecipeData == null) {
            if (!recipeFolder.exists()) {
                throw EntityNotFoundException(String.format("No recipe found for the given ID (%d)!", recipeNo))
            }
            return Recipe(-1)
        }
        val recipe = Recipe(recipeNo, jsonRecipeData?.name, jsonRecipeData?.notes, jsonRecipeData?.rating)
        if (recipeCatalogPicture != null) {
            recipe.catalogPicture = recipeCatalogPicture
        }
        if (recipeOwnPicture != null) {
            recipe.ownPicture = recipeOwnPicture
        }
        return recipe
    }

    private fun parseRecipeDataFromJson(recipeData: String): JsonRecipeData? =
        try {
            jacksonObjectMapper().readValue(recipeData)
        } catch (jsonParseException: JsonParseException) {
            null
        }

    fun readAllRecipes(): Collection<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val recipesFolder = File("${configurationService.recipeBasePath()}")
        recipesFolder.walk().maxDepth(1).forEach { file ->
            if (file.isDirectory && file.name.matches(Regex("recipe\\d{3}"))) {
                recipes.add(readRecipe(file.absolutePath));
            } else {
                if (file != recipesFolder) {
                    println("Skipped invalid folder '${file.name}'!")
                }
            }
        }
        return recipes.stream()
            .filter { it.isValidRecipe() }
            .sorted(compareBy(Recipe::id))
            .toList()
    }
}