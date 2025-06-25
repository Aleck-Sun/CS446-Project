package com.example.cs446.backend.data.model

enum class Species {
    DOG,
    CAT,
    GOLDFISH,
    HAMSTER,
    OTHER;

    override fun toString(): String {
        return name.lowercase()
            .split('_')
            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
    }
}