package com.mdymnoff.pokemonium.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mdymnoff.pokemonium.data.model.ui.UIState
import com.mdymnoff.pokemonium.viewmodels.common.BaseViewModel

/**
 * A composable that manages and displays UI based on the state from a [BaseViewModel].
 * It handles loading, success, and error states, reducing boilerplate in screen-level composables.
 *
 * This view observes the `uiState` from the provided [viewModel].
 * - In a `UIState.Loading` state, it sets the `isLoading` flag to true.
 * - In a `UIState.Success` state, it extracts the data and passes it to the `content` lambda.
 * - In a `UIState.Error` state, it displays an [ErrorBanner] with retry and dismiss actions.
 *
 * It provides a consistent structure for screens, including an optional app bar and the main content area.
 *
 * @param D The type of the data expected on a successful state.
 * @param modifier The [Modifier] to be applied to the root `Column` of this composable.
 * @param viewModel The instance of [BaseViewModel] that holds the UI state.
 * @param appBar An optional composable lambda for displaying a top app bar. Defaults to an empty composable.
 * @param content A composable lambda that receives the data (of type [D]?) and a boolean indicating if the view
 * is in a loading state. This is where the main UI for the screen should be defined.
 */
@Suppress("UNCHECKED_CAST")
@Composable
internal fun <D> StatefulView(
    modifier: Modifier = Modifier,
    viewModel: BaseViewModel,
    appBar: @Composable () -> Unit = {},
    content: @Composable (data: D?, isLoading: Boolean) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        val uiState by viewModel.uiState.collectAsState()
        val isLoading by remember { derivedStateOf { uiState is UIState.Loading } }
        val data by remember { derivedStateOf { (uiState as? UIState.Success<D>)?.data } }

        appBar()

        when (uiState) {
            is UIState.Error -> {
                ErrorBanner(
                    message = (uiState as UIState.Error).message,
                    onRetry = { viewModel.retry() },
                    onDismiss = { viewModel.dismissError() }
                )
            }

            else -> { /* no-op */ }
        }

        content(data, isLoading)
    }
}