package com.mdymnoff.pokemonium.viewmodels

import com.mdymnoff.pokemonium.data.model.backbone.DataResult
import com.mdymnoff.pokemonium.data.model.backbone.PokemonCard
import com.mdymnoff.pokemonium.data.model.backbone.PokemonPage
import com.mdymnoff.pokemonium.data.model.ui.UIState
import com.mdymnoff.pokemonium.data.model.ui.PokemonCardUI
import com.mdymnoff.pokemonium.data.repositories.PokemonRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {
    
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockRepository: PokemonRepository
    
    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk<PokemonRepository>()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `should load initial pokemon successfully`() = runTest(testDispatcher) {
        // Given
        val mockPokemonPage = PokemonPage(
            hasNext = true,
            hasPrevious = false,
            cards = listOf(
                PokemonCard(id = 1, name = "bulbasaur"),
                PokemonCard(id = 2, name = "ivysaur")
            )
        )
        
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(
            DataResult.Success(mockPokemonPage)
        )
        
        // When
        val viewModel = PokemonViewModelImpl(mockRepository)
        testScheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        uiState.shouldBeInstanceOf<UIState.Success<List<PokemonCardUI>>>()
        uiState.data.size shouldBe 2
        uiState.data[0].name shouldBe "bulbasaur"
        uiState.data[1].name shouldBe "ivysaur"
        viewModel.hasMore.value shouldBe true
        viewModel.isLoadingMore.value shouldBe false
    }
    
    @Test
    fun `should show error state when initialization fails`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException("Network error")
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(
            DataResult.Error(exception)
        )
        
        // When
        val viewModel = PokemonViewModelImpl(mockRepository)
        testScheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        uiState.shouldBeInstanceOf<UIState.Error>()
        uiState.message shouldBe "Network error"
        viewModel.hasMore.value shouldBe true
        viewModel.isLoadingMore.value shouldBe false
    }
    
    @Test
    fun `should append new pokemon when loadMore is called successfully`() = runTest(testDispatcher) {
        // Given
        val initialPage = PokemonPage(
            hasNext = true,
            hasPrevious = false,
            cards = listOf(PokemonCard(id = 1, name = "bulbasaur"))
        )
        
        val nextPage = PokemonPage(
            hasNext = true,
            hasPrevious = true,
            cards = listOf(PokemonCard(id = 2, name = "ivysaur"))
        )
        
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(DataResult.Success(initialPage))
        coEvery { mockRepository.getPage(10, 1) } returns flowOf(DataResult.Success(nextPage))
        
        val viewModel = PokemonViewModelImpl(mockRepository)
        
        // When
        testScheduler.advanceUntilIdle()
        
        val initialState = viewModel.uiState.value
        initialState.shouldBeInstanceOf<UIState.Success<List<PokemonCardUI>>>()
        initialState.data.size shouldBe 1
        
        viewModel.loadMore()
        testScheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        viewModel.isLoadingMore.value shouldBe false
        uiState.shouldBeInstanceOf<UIState.Success<List<PokemonCardUI>>>()
        uiState.data.size shouldBe 2
        uiState.data[0].name shouldBe "bulbasaur"
        uiState.data[1].name shouldBe "ivysaur"
    }
    
    @Test
    fun `should show error when loadMore fails`() = runTest(testDispatcher) {
        // Given
        val initialPage = PokemonPage(
            hasNext = true,
            hasPrevious = false,
            cards = listOf(PokemonCard(id = 1, name = "bulbasaur"))
        )
        
        val exception = RuntimeException("Load more failed")
        
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(DataResult.Success(initialPage))
        coEvery { mockRepository.getPage(10, 1) } returns flowOf(DataResult.Error(exception))
        
        val viewModel = PokemonViewModelImpl(mockRepository)
        testScheduler.advanceUntilIdle()
        
        // When
        viewModel.loadMore()
        testScheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        uiState.shouldBeInstanceOf<UIState.Error>()
        uiState.message shouldBe "Load more failed"
        viewModel.isLoadingMore.value shouldBe false
    }
    
    @Test
    fun `should not trigger loadMore when hasMore is false`() = runTest(testDispatcher) {
        // Given
        val initialPage = PokemonPage(
            hasNext = false,
            hasPrevious = false,
            cards = listOf(PokemonCard(id = 1, name = "bulbasaur"))
        )
        
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(DataResult.Success(initialPage))
        
        val viewModel = PokemonViewModelImpl(mockRepository)
        testScheduler.advanceUntilIdle()

        viewModel.hasMore.value shouldBe false
        
        // When
        viewModel.loadMore()
        testScheduler.advanceUntilIdle()
        
        // Then
        coVerify(exactly = 0) { mockRepository.getPage(10, 1) }
    }
    
    @Test
    fun `should call loadInitData when retry is called with empty list`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException("Initial error")
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(DataResult.Error(exception))
        
        val viewModel = PokemonViewModelImpl(mockRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.value.shouldBeInstanceOf<UIState.Error>()

        val successPage = PokemonPage(
            hasNext = false,
            hasPrevious = false,
            cards = listOf(PokemonCard(id = 1, name = "bulbasaur"))
        )
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(DataResult.Success(successPage))
        
        // When
        viewModel.retry()
        testScheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        uiState.shouldBeInstanceOf<UIState.Success<List<PokemonCardUI>>>()
        uiState.data.size shouldBe 1
        coVerify(atLeast = 2) { mockRepository.getPage(10, 0) }
    }
    
    @Test
    fun `should call loadMore when retry is called with existing data`() = runTest(testDispatcher) {
        // Given
        val initialPage = PokemonPage(
            hasNext = true,
            hasPrevious = false,
            cards = listOf(PokemonCard(id = 1, name = "bulbasaur"))
        )
        
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(DataResult.Success(initialPage))
        
        val viewModel = PokemonViewModelImpl(mockRepository)
        testScheduler.advanceUntilIdle()
        
        val exception = RuntimeException("Load more error")
        coEvery { mockRepository.getPage(10, 1) } returns flowOf(DataResult.Error(exception))
        
        viewModel.loadMore()
        testScheduler.advanceUntilIdle()
        
        viewModel.uiState.value.shouldBeInstanceOf<UIState.Error>()
        
        val nextPage = PokemonPage(
            hasNext = false,
            hasPrevious = true,
            cards = listOf(PokemonCard(id = 2, name = "ivysaur"))
        )
        coEvery { mockRepository.getPage(10, 1) } returns flowOf(DataResult.Success(nextPage))
        
        // When
        viewModel.retry()
        testScheduler.advanceUntilIdle()
        
        // Then
        coVerify(atLeast = 2) { mockRepository.getPage(10, 1) }
    }
    
    @Test
    fun `should clear error and show success state when dismissError is called with existing data`() = runTest(testDispatcher) {
        // Given
        val initialPage = PokemonPage(
            hasNext = true,
            hasPrevious = false,
            cards = listOf(PokemonCard(id = 1, name = "bulbasaur"))
        )
        
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(DataResult.Success(initialPage))
        
        val viewModel = PokemonViewModelImpl(mockRepository)
        testScheduler.advanceUntilIdle()
        
        val exception = RuntimeException("Load more error")
        coEvery { mockRepository.getPage(10, 1) } returns flowOf(DataResult.Error(exception))
        
        viewModel.loadMore()
        testScheduler.advanceUntilIdle()
        
        val errorState = viewModel.uiState.value
        errorState.shouldBeInstanceOf<UIState.Error>()
        
        // When
        viewModel.dismissError()
        
        // Then
        val dismissedState = viewModel.uiState.value
        dismissedState.shouldBeInstanceOf<UIState.Success<List<PokemonCardUI>>>()
        dismissedState.data.size shouldBe 1
    }
    
    @Test
    fun `should clear error and show idle state when dismissError is called with no existing data`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException("Initial load error")
        coEvery { mockRepository.getPage(10, 0) } returns flowOf(DataResult.Error(exception))
        
        val viewModel = PokemonViewModelImpl(mockRepository)
        testScheduler.advanceUntilIdle()
        
        val errorState = viewModel.uiState.value
        errorState.shouldBeInstanceOf<UIState.Error>()
        errorState.message shouldBe "Initial load error"
        
        // When
        viewModel.dismissError()
        
        // Then
        val dismissedState = viewModel.uiState.value
        dismissedState.shouldBeInstanceOf<UIState.Idle>()
    }
}