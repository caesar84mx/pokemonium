package com.mdymnoff.pokemonium.data.repositories

import com.mdymnoff.pokemonium.data.api_service.PokemonApiService
import com.mdymnoff.pokemonium.data.dao.PokemonDao
import com.mdymnoff.pokemonium.data.model.backbone.DataResult
import com.mdymnoff.pokemonium.data.model.backbone.PokemonPage
import com.mdymnoff.pokemonium.util.Constants.CACHE_EXPIRY_TIME_MS
import com.mdymnoff.pokemonium.util.Constants.POKEMON_CACHE_KEY
import com.mdymnoff.pokemonium.util.toBackbone
import com.mdymnoff.pokemonium.util.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Interface for data operations related to Pokémon.
 * This acts as an abstraction layer between the data sources (network, database)
 * and the rest of the application.
 */
interface PokemonRepository {
    /**
     * Fetches a paginated list of Pokemon.
     *
     * This function first attempts to retrieve the requested page from the local database cache.
     * If the cache is stale, invalid, or doesn't contain the full requested page, it fetches the
     * data from the remote API, updates the cache, and then returns the result.
     * The result is wrapped in a [Flow] of [DataResult] to handle loading, success, and error states.
     *
     * @param limit The maximum number of Pokemon to retrieve for the page.
     * @param offset The starting index for the pagination.
     * @return A [Flow] emitting a [DataResult] which contains a [PokemonPage] on success or an [Exception] on failure.
     */
    suspend fun getPage(limit: Int, offset: Int): Flow<DataResult<PokemonPage>>
}

class PokemonRepositoryImpl(
    private val apiService: PokemonApiService,
    private val pokemonDao: PokemonDao
) : PokemonRepository {
    
    override suspend fun getPage(limit: Int, offset: Int): Flow<DataResult<PokemonPage>> = flow {
        try {
            val cacheMetadata = pokemonDao.getCacheMetadata(POKEMON_CACHE_KEY)
            val currentTime = System.currentTimeMillis()
            val isCacheValid = cacheMetadata != null && (currentTime - cacheMetadata.lastUpdated) < CACHE_EXPIRY_TIME_MS
            
            val cachedCards = if (isCacheValid) {
                pokemonDao.getPokemonCards(limit, offset)
            } else {
                emptyList()
            }

            var totalCount: Int
            
            if (!isCacheValid || cachedCards.size < limit) {
                val page = apiService.getPokemonPage(limit = limit, offset = offset)
                totalCount = page.count
                
                val pokemonCards = page.results.map { it.toEntity() }

                if (!isCacheValid) {
                    pokemonDao.replaceAllPokemonCards(pokemonCards, totalCount)
                } else {
                    pokemonDao.addPokemonCards(pokemonCards, totalCount)
                }

                val cards = pokemonCards.map { it.toBackbone() }
                val hasNext = (offset + limit) < totalCount
                val hasPrevious = offset > 0
                
                emit(
                    DataResult.Success(
                        PokemonPage(
                            hasNext = hasNext,
                            hasPrevious = hasPrevious,
                            cards = cards
                        )
                    )
                )
            } else {
                val cards = cachedCards.map { it.toBackbone() }
                totalCount = cacheMetadata.totalCount
                
                val hasNext = (offset + limit) < totalCount
                val hasPrevious = offset > 0
                
                emit(
                    DataResult.Success(
                        PokemonPage(
                            hasNext = hasNext,
                            hasPrevious = hasPrevious,
                            cards = cards
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(DataResult.Error(e))
        }
    }
}
