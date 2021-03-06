package de.codinghaus.hellorating.recipe.model

data class Recipe(
    val id: Int,
    var name: String? = null,
    var notes: String? = null,
    var rating: Int? = null,
    var catalogPicture: String? = null,
    var ownPicture: String? = null
)