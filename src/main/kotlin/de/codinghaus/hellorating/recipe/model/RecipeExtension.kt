package de.codinghaus.hellorating.recipe.model

fun Recipe.isValidRecipe(): Boolean {
    return this.id > 0
}