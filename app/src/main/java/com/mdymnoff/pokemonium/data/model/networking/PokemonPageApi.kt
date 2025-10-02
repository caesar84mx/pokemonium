package com.mdymnoff.pokemonium.data.model.networking

import kotlinx.serialization.Serializable

@Serializable
data class PokemonPageApi(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<PokemonCardApi>,
)
