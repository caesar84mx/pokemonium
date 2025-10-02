package com.mdymnoff.pokemonium.data.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mdymnoff.pokemonium.util.Constants.POKEMON_CACHE_KEY
import com.mdymnoff.pokemonium.util.Constants.TABLE_CACHE_METADATA

@Entity(tableName = TABLE_CACHE_METADATA)
data class CacheMetadataEntity(
    @PrimaryKey
    val key: String,
    val totalCount: Int,
    val lastUpdated: Long,
) {
    companion object {
        fun createCacheMetadata(totalCount: Int): CacheMetadataEntity {
            return CacheMetadataEntity(
                key = POKEMON_CACHE_KEY,
                totalCount = totalCount,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
