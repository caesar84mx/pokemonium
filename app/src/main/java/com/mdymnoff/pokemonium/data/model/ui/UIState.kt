package com.mdymnoff.pokemonium.data.model.ui

sealed class UIState {
    object Loading : UIState()
    object Idle : UIState()
    data class Success<D>(val data: D) : UIState()
    data class Error(val message: String) : UIState()
}
