package com.example.cs446.backend.data.model

enum class Breed {
    GOLDEN_RETRIEVER,
    CAIRN_TERRIER,
    OTHER;

    fun raw(): String = name.lowercase()

    override fun toString(): String {
        return name.lowercase()
            .split('_')
            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
    }
}