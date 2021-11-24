package de.codinghaus.hellorating.configuration
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ConfigurationServiceTest {

    @Autowired
    private lateinit var configurationService: ConfigurationService

    @Test
    fun recipeBasePathIsFound() {
        val basePathFolder = configurationService.recipeBasePath()
        Assertions.assertThat(basePathFolder.path).isNotNull
        Assertions.assertThat(basePathFolder).isDirectory
    }
}