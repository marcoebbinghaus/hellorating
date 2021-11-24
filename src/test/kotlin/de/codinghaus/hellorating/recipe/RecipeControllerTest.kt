package de.codinghaus.hellorating.recipe

import de.codinghaus.hellorating.exception.model.ErrorType
import de.codinghaus.hellorating.exception.model.HttpError
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.INVALID_RECIPE_ID
import de.codinghaus.hellorating.recipe.TestObjects.RecipeTestValues.KOETBULLAR_WITH_PICS_ID
import de.codinghaus.hellorating.recipe.model.Recipe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RecipeControllerTest(@Autowired val restTemplate: TestRestTemplate) {

  @Test
  fun `Recipe by ID with unknown ID returns Not Found`() {
    val entity = restTemplate.getForEntity<HttpError>("/recipes/{recipeId}", INVALID_RECIPE_ID)
    assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(entity.body?.type).isEqualTo(ErrorType.ERROR)
    assertThat(entity.body?.message).isNotBlank
  }

  @Test
  fun `Recipe by ID with valid ID returns recipe`() {
    val entity = restTemplate.getForEntity<Recipe>("/recipes/{recipeId}", KOETBULLAR_WITH_PICS_ID)
    assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(entity.body?.id).isPositive
    assertThat(entity.body?.name).isNotBlank
    assertThat(entity.body?.notes).isNotBlank
    assertThat(entity.body?.rating).isPositive
    assertThat(entity.body?.catalogPicture).isNotBlank
    assertThat(entity.body?.ownPicture).isNotBlank
  }


}