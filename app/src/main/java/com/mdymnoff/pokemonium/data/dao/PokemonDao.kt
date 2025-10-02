package com.mdymnoff.pokemonium.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mdymnoff.pokemonium.data.model.entities.CacheMetadataEntity
import com.mdymnoff.pokemonium.data.model.entities.PokemonCardEntity
import com.mdymnoff.pokemonium.util.Constants.POKEMON_CACHE_KEY

@Dao
interface PokemonDao {
    
    @Query("SELECT * FROM pokemon_cards ORDER BY externalId LIMIT :limit OFFSET :offset")
    suspend fun getPokemonCards(limit: Int, offset: Int): List<PokemonCardEntity>
    
    @Query("SELECT * FROM pokemon_cards ORDER BY externalId")
    suspend fun getAllPokemonCards(): List<PokemonCardEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonCards(cards: List<PokemonCardEntity>)
    
    @Query("DELETE FROM pokemon_cards")
    suspend fun clearPokemonCards()
    
    @Query("SELECT * FROM cache_metadata WHERE key = :key")
    suspend fun getCacheMetadata(key: String): CacheMetadataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheMetadata(metadata: CacheMetadataEntity)
    
    @Query("DELETE FROM cache_metadata WHERE key = :key")
    suspend fun deleteCacheMetadata(key: String)
    
    @Transaction
    suspend fun replaceAllPokemonCards(cards: List<PokemonCardEntity>, totalCount: Int) {
        clearPokemonCards()
        insertPokemonCards(cards)
        insertCacheMetadata(CacheMetadataEntity.createCacheMetadata(totalCount))
    }
    
    @Transaction
    suspend fun addPokemonCards(cards: List<PokemonCardEntity>, totalCount: Int) {
        val existingCards = getAllPokemonCards()
        val existingExternalIds = existingCards.map { it.externalId }.toSet()
        val newCards = cards.filter { it.externalId !in existingExternalIds }
        
        if (newCards.isNotEmpty()) {
            insertPokemonCards(newCards)
        }
        insertCacheMetadata(CacheMetadataEntity.createCacheMetadata(totalCount))
    }
    
    @Transaction
    suspend fun clearAllCache() {
        clearPokemonCards()
        deleteCacheMetadata(POKEMON_CACHE_KEY)
    }
}