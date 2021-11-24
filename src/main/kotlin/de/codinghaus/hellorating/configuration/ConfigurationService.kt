package de.codinghaus.hellorating.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class ConfigurationService {

    @Value("\${hellofresh.configuration.recipes.basepath}")
    private lateinit var basePath: String

    fun recipeBasePath(): File {
        return File(basePath)
    }
}