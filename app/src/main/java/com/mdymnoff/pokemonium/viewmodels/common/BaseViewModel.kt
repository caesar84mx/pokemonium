package com.mdymnoff.pokemonium.viewmodels.common

import androidx.lifecycle.ViewModel
import com.mdymnoff.pokemonium.data.model.ui.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * An abstract base class for ViewModels that manage a common UI state.
 *
 * This class provides a basic structure for handling UI states like Idle, Loading, Success, and Error.
 * It exposes a [StateFlow] of [UIState] that UI components can observe to react to changes.
 * Concrete ViewModel implementations should extend this class and provide logic for retrying failed
 * operations and dismissing errors.
 *
 * @property _uiState A protected [MutableStateFlow] to internally manage the current [UIState].
 * @property uiState A public, read-only [StateFlow] exposing the current [UIState] to the UI.
 */
abstract class BaseViewModel: ViewModel() {
    protected val _uiState = MutableStateFlow<UIState>(UIState.Idle)
    val uiState: StateFlow<UIState> = _uiState

    /**
     * Retries the last failed operation.
     * This function should be implemented by subclasses to re-trigger the logic
     * that led to an error state, typically an API call or a data-fetching process.
     */
    abstract fun retry()

    /**
     * Dismisses the current error state, typically by transitioning the UI state
     * back to a non-error state (e.g., Idle or showing previous content).
     */
    abstract fun dismissError()
}