package com.mdymnoff.pokemonium.util

import com.mdymnoff.pokemonium.BuildConfig
import com.mdymnoff.pokemonium.data.model.backbone.PokemonCard
import com.mdymnoff.pokemonium.data.model.entities.PokemonCardEntity
import com.mdymnoff.pokemonium.data.model.networking.PokemonCardApi
import com.mdymnoff.pokemonium.data.model.ui.PokemonCardUI
import com.mdymnoff.pokemonium.util.Constants.URL_PARTS_ID_INDEX_FROM_END

val PokemonCard.imageUrl: String
    get() = BuildConfig.DEFAULT_AVATAR_BASE_URL + "$id.png"

fun PokemonCard.toUI(): PokemonCardUI = PokemonCardUI(
    name = name,
    imageUrl = imageUrl,
)

fun PokemonCardApi.toEntity(): PokemonCardEntity {
    val urlParts = url.split("/")
    val externalId = urlParts[urlParts.size - URL_PARTS_ID_INDEX_FROM_END].toInt()

    return PokemonCardEntity(
        name = name,
        url = url,
        externalId = externalId
    )
}

fun PokemonCardEntity.toBackbone(): PokemonCard {
    return PokemonCard(
        id = externalId,
        name = name,
    )
}