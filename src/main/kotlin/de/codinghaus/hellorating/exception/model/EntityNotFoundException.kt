package de.codinghaus.hellorating.exception.model

class EntityNotFoundException(override val message: String) : RuntimeException(message)