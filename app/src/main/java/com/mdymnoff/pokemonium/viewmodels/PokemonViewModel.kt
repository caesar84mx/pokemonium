package com.mdymnoff.pokemonium.viewmodels

import androidx.lifecycle.viewModelScope
import com.mdymnoff.pokemonium.data.model.backbone.PokemonCard
import com.mdymnoff.pokemonium.data.model.backbone.onError
import com.mdymnoff.pokemonium.data.model.backbone.onSuccess
import com.mdymnoff.pokemonium.data.model.ui.UIState
import com.mdymnoff.pokemonium.data.repositories.PokemonRepository
import com.mdymnoff.pokemonium.util.Constants.PAGE_SIZE
import com.mdymnoff.pokemonium.util.Constants.ERROR_UNKNOWN
import com.mdymnoff.pokemonium.util.Constants.ERROR_LOAD_MORE_POKEMON
import com.mdymnoff.pokemonium.util.toUI
import com.mdymnoff.pokemonium.viewmodels.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class PokemonViewModel : BaseViewModel() {
    abstract val isLoadingMore: StateFlow<Boolean>
    abstract val hasMore: StateFlow<Boolean>
    abstract fun loadInitData()
    abstract fun loadMore()
}

internal class PokemonViewModelImpl(
    private val repository: PokemonRepository
) : PokemonViewModel() {
    private val _isLoadingMore = MutableStateFlow(false)
    override val isLoadingMore = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    override val hasMore = _hasMore.asStateFlow()

    private var currentOffset = 0
    private val pageSize = PAGE_SIZE
    private val currentList = mutableListOf<PokemonCard>()

    init {
        loadInitData()
    }

    override fun loadInitData() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            _isLoadingMore.value = false
            currentOffset = 0
            currentList.clear()

            repository.getPage(pageSize, currentOffset).collect { result ->
                result
                    .onSuccess { page ->
                        currentList.addAll(page.cards)
                        currentOffset += page.cards.size
                        _hasMore.value = page.hasNext
                        _uiState.value = UIState.Success(currentList.map { it.toUI() })
                    }
                    .onError { exception ->
                        val errorMessage = exception.message ?: ERROR_UNKNOWN
                        _uiState.value = UIState.Error(errorMessage)
                    }
            }
        }
    }

    override fun loadMore() {
        if (_isLoadingMore.value || !_hasMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true

            repository.getPage(pageSize, currentOffset).collect { result ->
                result
                    .onSuccess { page ->
                        currentList.addAll(page.cards)
                        currentOffset += page.cards.size
                        _hasMore.value = page.hasNext
                        _isLoadingMore.value = false
                        _uiState.value = UIState.Success(currentList.map { it.toUI() })
                    }
                    .onError { exception ->
                        val errorMessage = exception.message ?: ERROR_LOAD_MORE_POKEMON
                        _uiState.value = UIState.Error(errorMessage)
                        _isLoadingMore.value = false
                    }
            }
        }
    }

    override fun retry() {
        val currentState = _uiState.value
        if (currentState is UIState.Error) {
            if (currentList.isEmpty()) {
                loadInitData()
            } else {
                loadMore()
            }
        }
    }

    override fun dismissError() {
        val currentState = _uiState.value
        if (currentState is UIState.Error) {
            if (currentList.isNotEmpty()) {
                _uiState.value = UIState.Success(currentList.toList())
            } else {
                _uiState.value = UIState.Idle
            }
        }
    }
}
