package com.mdymnoff.pokemonium.data.model.backbone

data class PokemonPage(
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val cards: List<PokemonCard>,
)
