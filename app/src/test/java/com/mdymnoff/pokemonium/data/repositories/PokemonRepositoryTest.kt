package com.mdymnoff.pokemonium.data.repositories

import com.mdymnoff.pokemonium.data.api_service.PokemonApiService
import com.mdymnoff.pokemonium.data.dao.PokemonDao
import com.mdymnoff.pokemonium.data.model.backbone.DataResult
import com.mdymnoff.pokemonium.data.model.backbone.PokemonPage
import com.mdymnoff.pokemonium.data.model.entities.CacheMetadataEntity
import com.mdymnoff.pokemonium.data.model.entities.PokemonCardEntity
import com.mdymnoff.pokemonium.data.model.networking.PokemonCardApi
import com.mdymnoff.pokemonium.data.model.networking.PokemonPageApi
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PokemonRepositoryTest {
    
    private lateinit var mockApiService: PokemonApiService
    private lateinit var mockPokemonDao: PokemonDao
    private lateinit var repository: PokemonRepositoryImpl
    
    @Before
    fun setUp() {
        mockApiService = mockk<PokemonApiService>()
        mockPokemonDao = mockk<PokemonDao>(relaxed = true)
        repository = PokemonRepositoryImpl(mockApiService, mockPokemonDao)
    }
    
    @Test
    fun `should return cached data when cache is valid and has enough data`() = runTest {
        // Given
        val cacheMetadata = CacheMetadataEntity(
            key = "pokemon_cache",
            totalCount = 1302,
            lastUpdated = System.currentTimeMillis() - 1000L
        )
        
        val cachedEntities = listOf(
            PokemonCardEntity(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/", externalId = 1),
            PokemonCardEntity(name = "ivysaur", url = "https://pokeapi.co/api/v2/pokemon/2/", externalId = 2)
        )
        
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns cacheMetadata
        coEvery { mockPokemonDao.getPokemonCards(2, 0) } returns cachedEntities
        
        // When
        val flow = repository.getPage(2, 0)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Success<PokemonPage>>()
        result.data.cards.size shouldBe 2
        result.data.cards[0].name shouldBe "bulbasaur"
        result.data.cards[1].name shouldBe "ivysaur"
        result.data.hasNext shouldBe true
        result.data.hasPrevious shouldBe false
        
        coVerify(exactly = 0) { mockApiService.getPokemonPage(any(), any()) }
    }
    
    @Test
    fun `should fetch from network when cache is expired`() = runTest {
        // Given
        val expiredCacheMetadata = CacheMetadataEntity(
            key = "pokemon_cache",
            totalCount = 2,
            lastUpdated = System.currentTimeMillis() - (10 * 60 * 1000L)
        )
        
        val mockResponse = PokemonPageApi(
            count = 1302,
            next = "https://pokeapi.co/api/v2/pokemon/?offset=10&limit=10",
            previous = null,
            results = listOf(
                PokemonCardApi("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonCardApi("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
            )
        )
        
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns expiredCacheMetadata
        coEvery { mockPokemonDao.getPokemonCards(10, 0) } returns emptyList()
        coEvery { mockApiService.getPokemonPage(10, 0) } returns mockResponse
        
        // When
        val flow = repository.getPage(10, 0)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Success<PokemonPage>>()
        result.data.cards.size shouldBe 2
        result.data.hasNext shouldBe true
        result.data.hasPrevious shouldBe false
        
        coVerify { mockApiService.getPokemonPage(10, 0) }
        coVerify { mockPokemonDao.replaceAllPokemonCards(any(), 1302) }
    }
    
    @Test
    fun `should fetch from network when no cache exists`() = runTest {
        // Given
        val mockResponse = PokemonPageApi(
            count = 1302,
            next = "https://pokeapi.co/api/v2/pokemon/?offset=10&limit=10",
            previous = null,
            results = listOf(
                PokemonCardApi("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonCardApi("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
            )
        )
        
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns null
        coEvery { mockPokemonDao.getPokemonCards(10, 0) } returns emptyList()
        coEvery { mockApiService.getPokemonPage(10, 0) } returns mockResponse
        
        // When
        val flow = repository.getPage(10, 0)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Success<PokemonPage>>()
        result.data.cards.size shouldBe 2
        result.data.cards[0].name shouldBe "bulbasaur"
        result.data.hasNext shouldBe true
        result.data.hasPrevious shouldBe false
        
        coVerify { mockApiService.getPokemonPage(10, 0) }
        coVerify { mockPokemonDao.replaceAllPokemonCards(any(), 1302) }
    }
    
    @Test
    fun `should fetch missing data from network when cache is valid but insufficient`() = runTest {
        // Given
        val validCacheMetadata = CacheMetadataEntity(
            key = "pokemon_cache",
            totalCount = 1302,
            lastUpdated = System.currentTimeMillis() - 1000L
        )
        
        val mockResponse = PokemonPageApi(
            count = 1302,
            next = "https://pokeapi.co/api/v2/pokemon/?offset=20&limit=10",
            previous = "https://pokeapi.co/api/v2/pokemon/?offset=0&limit=10",
            results = listOf(
                PokemonCardApi("charmander", "https://pokeapi.co/api/v2/pokemon/4/"),
                PokemonCardApi("charmeleon", "https://pokeapi.co/api/v2/pokemon/5/")
            )
        )
        
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns validCacheMetadata
        coEvery { mockPokemonDao.getPokemonCards(10, 10) } returns emptyList()
        coEvery { mockApiService.getPokemonPage(10, 10) } returns mockResponse
        
        // When
        val flow = repository.getPage(10, 10)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Success<PokemonPage>>()
        result.data.cards.size shouldBe 2
        result.data.cards[0].name shouldBe "charmander"
        result.data.hasNext shouldBe true
        result.data.hasPrevious shouldBe true
        
        coVerify { mockApiService.getPokemonPage(10, 10) }
        coVerify { mockPokemonDao.addPokemonCards(any(), 1302) }
    }
    
    @Test
    fun `should handle partial cached data correctly`() = runTest {
        // Given
        val validCacheMetadata = CacheMetadataEntity(
            key = "pokemon_cache",
            totalCount = 1302,
            lastUpdated = System.currentTimeMillis() - 1000L
        )
        
        val cachedEntities = listOf(
            PokemonCardEntity(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/", externalId = 1)
        )
        
        val mockResponse = PokemonPageApi(
            count = 1302,
            next = "https://pokeapi.co/api/v2/pokemon/?offset=10&limit=10",
            previous = null,
            results = listOf(
                PokemonCardApi("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonCardApi("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
            )
        )
        
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns validCacheMetadata
        coEvery { mockPokemonDao.getPokemonCards(10, 0) } returns cachedEntities // Less than requested
        coEvery { mockApiService.getPokemonPage(10, 0) } returns mockResponse
        
        // When
        val flow = repository.getPage(10, 0)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Success<PokemonPage>>()
        result.data.cards.size shouldBe 2
        
        coVerify { mockApiService.getPokemonPage(10, 0) }
        coVerify { mockPokemonDao.addPokemonCards(any(), 1302) }
    }
    
    @Test
    fun `should calculate hasNext correctly for last page`() = runTest {
        // Given
        val mockResponse = PokemonPageApi(
            count = 25, // Total count
            next = null, // No next page
            previous = "https://pokeapi.co/api/v2/pokemon/?offset=10&limit=10",
            results = listOf(
                PokemonCardApi("pokemon21", "https://pokeapi.co/api/v2/pokemon/21/"),
                PokemonCardApi("pokemon22", "https://pokeapi.co/api/v2/pokemon/22/"),
                PokemonCardApi("pokemon23", "https://pokeapi.co/api/v2/pokemon/23/"),
                PokemonCardApi("pokemon24", "https://pokeapi.co/api/v2/pokemon/24/"),
                PokemonCardApi("pokemon25", "https://pokeapi.co/api/v2/pokemon/25/")
            )
        )
        
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns null
        coEvery { mockPokemonDao.getPokemonCards(10, 20) } returns emptyList()
        coEvery { mockApiService.getPokemonPage(10, 20) } returns mockResponse
        
        // When
        val flow = repository.getPage(10, 20)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Success<PokemonPage>>()
        result.data.cards.size shouldBe 5
        result.data.hasNext shouldBe false
        result.data.hasPrevious shouldBe true
    }
    
    @Test
    fun `should calculate hasPrevious correctly for first page`() = runTest {
        // Given
        val mockResponse = PokemonPageApi(
            count = 1302,
            next = "https://pokeapi.co/api/v2/pokemon/?offset=10&limit=10",
            previous = null,
            results = listOf(
                PokemonCardApi("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonCardApi("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
            )
        )
        
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns null
        coEvery { mockPokemonDao.getPokemonCards(10, 0) } returns emptyList()
        coEvery { mockApiService.getPokemonPage(10, 0) } returns mockResponse
        
        // When
        val flow = repository.getPage(10, 0) // First page
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Success<PokemonPage>>()
        result.data.hasNext shouldBe true
        result.data.hasPrevious shouldBe false
    }
    
    @Test
    fun `should handle network error gracefully`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns null
        coEvery { mockPokemonDao.getPokemonCards(10, 0) } returns emptyList()
        coEvery { mockApiService.getPokemonPage(10, 0) } throws exception
        
        // When
        val flow = repository.getPage(10, 0)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Error>()
        result.exception.message shouldBe "Network error"
        
        coVerify { mockApiService.getPokemonPage(10, 0) }
    }
    
    @Test
    fun `should handle database error gracefully`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } throws exception
        
        // When
        val flow = repository.getPage(10, 0)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Error>()
        result.exception.message shouldBe "Database error"
    }
    
    @Test
    fun `should not duplicate pokemon when adding to valid cache`() = runTest {
        // Given
        val validCacheMetadata = CacheMetadataEntity(
            key = "pokemon_cache",
            totalCount = 1302,
            lastUpdated = System.currentTimeMillis() - 1000L
        )
        
        val existingCachedEntities = listOf(
            PokemonCardEntity(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/", externalId = 1),
            PokemonCardEntity(name = "ivysaur", url = "https://pokeapi.co/api/v2/pokemon/2/", externalId = 2)
        )
        
        val mockResponse = PokemonPageApi(
            count = 1302,
            next = "https://pokeapi.co/api/v2/pokemon/?offset=20&limit=10",
            previous = "https://pokeapi.co/api/v2/pokemon/?offset=0&limit=10",
            results = listOf(
                PokemonCardApi("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonCardApi("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/"),
                PokemonCardApi("venusaur", "https://pokeapi.co/api/v2/pokemon/3/"),
                PokemonCardApi("charmander", "https://pokeapi.co/api/v2/pokemon/4/")
            )
        )
        
        coEvery { mockPokemonDao.getCacheMetadata("pokemon_cache") } returns validCacheMetadata
        coEvery { mockPokemonDao.getPokemonCards(10, 10) } returns emptyList()
        coEvery { mockPokemonDao.getAllPokemonCards() } returns existingCachedEntities
        coEvery { mockApiService.getPokemonPage(10, 10) } returns mockResponse
        
        // When
        val flow = repository.getPage(10, 10)
        val results = flow.toList()
        
        // Then
        results.size shouldBe 1
        val result = results[0]
        result.shouldBeInstanceOf<DataResult.Success<PokemonPage>>()
        result.data.cards.size shouldBe 4

        coVerify { mockPokemonDao.addPokemonCards(any(), 1302) }
        coVerify { mockApiService.getPokemonPage(10, 10) }
    }
}