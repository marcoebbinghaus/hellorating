package de.codinghaus.hellorating.recipe
import de.codinghaus.hellorating.configuration.ConfigurationService
import de.codinghaus.hellorating.exception.model.EntityNotFoundException
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.BROKEN_JSON_RECIPE
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.ERROR_ID
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.INVALID_RECIPE_ID
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.KOETBULLAR_WITH_PICS_ID
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.NO_JSON_RECIPE
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.PIZZA_WITHOUT_PICS_ID
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.SPAGHETTI_WITH_PICS_ID
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.VALID_RECIPES_COUNT
import de.codinghaus.hellorating.recipe.model.isValidRecipe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File

@SpringBootTest
@ActiveProfiles("test")
class RecipeServiceTest(@Autowired val configurationService: ConfigurationService) {

    @Autowired
    private lateinit var recipeService: RecipeService

    var testFilesRoot = configurationService.recipeBasePath()
    var testFilesRootTemp = File("src/test/resources/tmp")

    @BeforeEach
    fun setup() {
        testFilesRoot.copyRecursively(testFilesRootTemp)
    }

    @AfterEach
    fun tearDown() {
        testFilesRoot.deleteRecursively()
        testFilesRootTemp.copyRecursively(testFilesRoot, overwrite = true)
        testFilesRootTemp.deleteRecursively()
    }

    @Test
    fun `readRecipe with magharita pizza recipe ID returns full valid recipe`() {
        val recipe = recipeService.readRecipeById(PIZZA_WITHOUT_PICS_ID)
        assertThat(recipe.isValidRecipe()).isTrue
        assertThat(recipe.id).isEqualTo(PIZZA_WITHOUT_PICS_ID)
        assertThat(recipe.name).isNotBlank
        assertThat(recipe.notes).isNotBlank
        assertThat(recipe.rating).isNotNull
        assertThat(recipe.catalogPicture).isNull()
        assertThat(recipe.ownPicture).isNull()
    }

    @Test
    fun `readRecipe with koetbullar recipe ID returns full valid recipe`() {
        val recipe = recipeService.readRecipeById(KOETBULLAR_WITH_PICS_ID)
        assertThat(recipe.isValidRecipe()).isTrue
        assertThat(recipe.id).isEqualTo(KOETBULLAR_WITH_PICS_ID)
        assertThat(recipe.name).isNotBlank
        assertThat(recipe.notes).isNotBlank
        assertThat(recipe.rating).isNotNull
        assertThat(recipe.catalogPicture).isNotNull
        assertThat(recipe.ownPicture).isNotNull
    }

    @Test
    fun `readRecipe with spaghetti recipe ID returns full valid recipe`() {
        val recipe = recipeService.readRecipeById(SPAGHETTI_WITH_PICS_ID)
        assertThat(recipe.isValidRecipe()).isTrue
        assertThat(recipe.id).isEqualTo(SPAGHETTI_WITH_PICS_ID)
        assertThat(recipe.name).isNotBlank
        assertThat(recipe.notes).isNotBlank
        assertThat(recipe.rating).isNotNull
        assertThat(recipe.catalogPicture).isNotNull
        assertThat(recipe.ownPicture).isNotNull
    }

    @Test
    fun `readRecipe with no broken returns ErrorRecipe`() {
        val recipe = recipeService.readRecipeById(BROKEN_JSON_RECIPE)
        assertThat(recipe.id).isEqualTo(ERROR_ID)
        assertThat(recipe.isValidRecipe()).isFalse
        assertThat(recipe.name).isNull()
        assertThat(recipe.notes).isNull()
        assertThat(recipe.rating).isNull()
        assertThat(recipe.catalogPicture).isNull()
        assertThat(recipe.ownPicture).isNull()
    }

    @Test
    fun `readRecipe with no json returns ErrorRecipe`() {
        val recipe = recipeService.readRecipeById(NO_JSON_RECIPE)
        assertThat(recipe.id).isEqualTo(ERROR_ID)
        assertThat(recipe.isValidRecipe()).isFalse
        assertThat(recipe.name).isNull()
        assertThat(recipe.notes).isNull()
        assertThat(recipe.rating).isNull()
        assertThat(recipe.catalogPicture).isNull()
        assertThat(recipe.ownPicture).isNull()
    }

    @Test
    fun `readRecipe with non-existing ID throws Exception`() {
        assertThrows(EntityNotFoundException::class.java) {
            recipeService.readRecipeById(INVALID_RECIPE_ID)
        }
    }

    @Test
    fun `readAllRecipes works correctly`() {
        val recipes = recipeService.readAllRecipes()
        assertThat(recipes.size).isEqualTo(VALID_RECIPES_COUNT)
        recipes.stream().forEach { assertThat(it.id).isGreaterThan(0) }
    }

    @Test
    fun `updateRecipe works correctly`() {
        val recipeBeforeUpdate = recipeService.readRecipeById(SPAGHETTI_WITH_PICS_ID)

        val recipeAfterUpdate = recipeService.updateRecipeRating(SPAGHETTI_WITH_PICS_ID, 5)

        assertThat(recipeBeforeUpdate.id).isEqualTo(recipeAfterUpdate.id)
        assertThat(recipeBeforeUpdate.name).isEqualTo(recipeAfterUpdate.name)
        assertThat(recipeBeforeUpdate.notes).isEqualTo(recipeAfterUpdate.notes)
        assertThat(recipeBeforeUpdate.catalogPicture).isEqualTo(recipeAfterUpdate.catalogPicture)
        assertThat(recipeBeforeUpdate.ownPicture).isEqualTo(recipeAfterUpdate.ownPicture)
        assertThat(recipeBeforeUpdate.rating).isNotEqualTo(recipeAfterUpdate.rating)
        assertThat(recipeAfterUpdate.rating).isEqualTo(5)
    }

    @Test
    fun `updateRecipe with non-existing ID throws Exception`() {
        assertThrows(EntityNotFoundException::class.java) {
            recipeService.updateRecipeRating(INVALID_RECIPE_ID, 5)
        }
    }

    @Test
    fun `create works correctly`() {
        val createdRecipe = recipeService.createRecipe("Tacos Mexicana", "sehr geil.", 8)

        assertThat(File(testFilesRoot.absolutePath + "/recipe004")).exists();
        assertThat(createdRecipe.id).isNotNull
        assertThat(createdRecipe.name).isEqualTo("Tacos Mexicana")
        assertThat(createdRecipe.notes).isEqualTo("sehr geil.")
        assertThat(createdRecipe.rating).isEqualTo( 8)
        assertThat(createdRecipe.ownPicture).isNull()
        assertThat(createdRecipe.catalogPicture).isNull()
    }
}