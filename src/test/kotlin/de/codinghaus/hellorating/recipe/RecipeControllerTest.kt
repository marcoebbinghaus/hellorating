package de.codinghaus.hellorating.recipe

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.codinghaus.hellorating.configuration.ConfigurationService
import de.codinghaus.hellorating.exception.model.ErrorType
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.INVALID_RECIPE_ID
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.KOETBULLAR_WITH_PICS_ID
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.VALID_RECIPES_COUNT
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.io.File

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RecipeControllerTest(
    @Autowired val configurationService: ConfigurationService
) {
    @Autowired
    private lateinit var mockMvc: MockMvc

    var testFilesRoot = configurationService.recipeBasePath()
    var testFilesRootTemp = File("src/test/resources/tmp")

    @BeforeEach
    fun setup() {
        testFilesRoot.copyRecursively(testFilesRootTemp)
    }

    @AfterEach
    fun tearDown() {
        testFilesRootTemp.copyRecursively(testFilesRoot, overwrite = true)
        testFilesRootTemp.deleteRecursively()
    }

    @Test
    fun `Recipe by ID with unknown ID returns Not Found`() {
        mockMvc.get("/recipes/${INVALID_RECIPE_ID}") {
        }.andExpect {
            status { isNotFound() }
            content { jsonPath("\$.type", equalTo(ErrorType.ERROR.name)) }
            content { jsonPath("\$.message", equalTo("No recipe found for the given ID (${INVALID_RECIPE_ID})!")) }
        }
    }

    @Test
    fun `Recipe by ID with valid ID returns recipe`() {
        mockMvc.get("/recipes/${KOETBULLAR_WITH_PICS_ID}") {
        }.andExpect {
            status { is2xxSuccessful() }
            content { jsonPath("\$.id", greaterThan(0)) }
            content { jsonPath("\$.name", equalTo("Kötbullar")) }
            content { jsonPath("\$.notes", equalTo("Lief gut soweit!")) }
            content { jsonPath("\$.rating", equalTo(8)) }
            content { jsonPath("\$.catalogPicture", notNullValue()) }
            content { jsonPath("\$.ownPicture", notNullValue()) }
        }
    }

    @Test
    fun `getAllRecipes returns all valid recipes`() {
        mockMvc.get("/recipes") {
        }.andExpect {
            status { is2xxSuccessful() }
            content { jsonPath("\$.length()", `is`(VALID_RECIPES_COUNT)) }
        }
    }

    @Test
    fun `Update Rating for recipe works`() {
        mockMvc.get("/recipes/${KOETBULLAR_WITH_PICS_ID}")
            .andExpect { status { is2xxSuccessful() } }
            .andExpect { jsonPath("\$.rating").value(equals(2)) }

        mockMvc.patch("/recipes/${KOETBULLAR_WITH_PICS_ID}") {
            contentType = MediaType.APPLICATION_JSON
            content = jacksonObjectMapper().writeValueAsString(hashMapOf(Pair("rating", 5)))
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { is2xxSuccessful() }
            content { jsonPath("\$.rating", equalTo(5)) }
        }
    }

    @Test
    fun `Update Rating for unknown recipe fails with 404`() {
        mockMvc.patch("/recipes/${INVALID_RECIPE_ID}") {
            contentType = MediaType.APPLICATION_JSON
            content = jacksonObjectMapper().writeValueAsString(hashMapOf(Pair("rating", 5)))
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { json("""{"type":"ERROR","message":"No recipe found for the given ID (${INVALID_RECIPE_ID})!"}""") }
        }
    }
}