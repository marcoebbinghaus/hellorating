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
import java.nio.charset.Charset

@Component
class RecipeService(val configurationService: ConfigurationService) {

    val recipeSubPathPattern = "/recipe%03d"

    fun readAllRecipes(): Collection<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val recipesFolder = File("${configurationService.recipeBasePath()}")
        recipesFolder.walk().maxDepth(1).forEach { file ->
            if (file.isDirectory && file.name.matches(Regex("recipe\\d{3}"))) {
                recipes.add(readRecipeByPath(file.absolutePath));
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

    fun readRecipeById(id: Int): Recipe {
        val completePath = fetchCompletePathForRecipeById(id)
        return readRecipeByPath(completePath)
    }

    fun updateRecipeRating(recipeId: Int, rating: Int): Recipe {
        if (rating < 0 || rating > 10) {
            throw IllegalArgumentException("rating must be a number from 0 to 10!")
        }
        val recipeFolder = fetchFileForRecipeById(recipeId)
        val recipe = readRecipeById(recipeId);
        recipe.rating = rating
        applyRecipeDataToFile(recipe, recipeFolder)
        return readRecipeById(recipe.id)
    }
    fun createRecipe(name: String, notes: String = "", rating: Int): Recipe {
        val existingRecipeCount = fetchRecipeFolderCount()
        val newRecipeFolder = File("${configurationService.recipeBasePath()}/${String.format(recipeSubPathPattern, existingRecipeCount + 1)}")
        val recipeData = createRecipeData(name, notes, rating)
        File("${newRecipeFolder.absolutePath}/recipe-data.json").printWriter().use { out ->
            out.print(recipeData)
        }
        return readRecipeById(Integer.parseInt(newRecipeFolder.name.takeLast(3)))
    }

    private fun createRecipeData(name: String, notes: String = "", rating: Int): String {
        return jacksonObjectMapper().writeValueAsString(JsonRecipeData(name, notes, rating))
    }

    private fun fetchRecipeFolderCount(): Int {
        return readAllRecipes().size
    }

    private fun fetchFileForRecipeById(id: Int): File {
        val completePath = fetchCompletePathForRecipeById(id)
        return File(completePath)
    }

    private fun fetchCompletePathForRecipeById(id: Int): String {
        return "${configurationService.recipeBasePath()}" + String.format(recipeSubPathPattern, id)
    }

    private fun readRecipeByPath(completePathToRecipeFolder: String): Recipe {
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

    private fun applyRecipeDataToFile(recipe: Recipe, recipeFolder: File) {
        val recipeData = hashMapOf(Pair("name", recipe.name ?: ""), Pair("notes", recipe.notes ?: ""), Pair("rating", recipe.rating ?: 0))
        File(recipeFolder, "recipe-data.json").writeText(jacksonObjectMapper().writeValueAsString(recipeData), Charset.forName("UTF8"))
    }

    private fun writeContentToFile(fileContent: String, fileName: String, recipeFolder: File) {
        val ownPictureFile = recipeFolder.listFiles().find { it.absolutePath.endsWith(fileName) } ?: File(
            recipeFolder,
            fileName
        )
        ownPictureFile.printWriter().use { out ->
            out.print(Base64.decodeBase64(fileContent))
        }
    }
}