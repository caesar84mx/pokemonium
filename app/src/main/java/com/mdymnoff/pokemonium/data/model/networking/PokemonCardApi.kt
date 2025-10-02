package com.mdymnoff.pokemonium.data.model.networking

import kotlinx.serialization.Serializable

@Serializable
data class PokemonCardApi(
    val name: String,
    val url: String,
)
