package com.mdymnoff.pokemonium.data.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mdymnoff.pokemonium.util.Constants.TABLE_POKEMON_CARDS

@Entity(tableName = TABLE_POKEMON_CARDS)
data class PokemonCardEntity(
    @PrimaryKey
    val externalId: Int,
    val name: String,
    val url: String,
)
