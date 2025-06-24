package com.example.cs446.backend.data.model

enum class Breed {
    // Dog
    CAIRN_TERRIER,
    CHIHUAHUA,
    GOLDEN_RETRIEVER,
    POODLE,

    // Cat
    AMERICAN_SHORTHAIR,
    BRITISH_LONGHAIR,
    SIAMESE,

    // Hamster
    DWARF,
    SYRIAN,
    WINTER_WHITE,

    OTHER;

    override fun toString(): String {
        return name.lowercase()
            .split('_')
            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
    }
}

fun speciesOfBreed(breed: Breed): Species = when (breed) {
    Breed.GOLDEN_RETRIEVER, Breed.CAIRN_TERRIER, Breed.CHIHUAHUA, Breed.POODLE -> Species.DOG
    Breed.AMERICAN_SHORTHAIR, Breed.BRITISH_LONGHAIR, Breed.SIAMESE -> Species.CAT
    Breed.DWARF, Breed.SYRIAN, Breed.WINTER_WHITE -> Species.HAMSTER
    Breed.OTHER -> Species.OTHER
}
