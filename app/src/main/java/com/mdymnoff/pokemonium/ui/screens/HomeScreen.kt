package com.mdymnoff.pokemonium.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.mdymnoff.pokemonium.R
import com.mdymnoff.pokemonium.data.model.ui.PokemonCardUI
import com.mdymnoff.pokemonium.ui.components.InfiniteScrollList
import com.mdymnoff.pokemonium.ui.components.PokemonCard
import com.mdymnoff.pokemonium.ui.components.StatefulView
import com.mdymnoff.pokemonium.viewmodels.PokemonViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: PokemonViewModel = koinViewModel()
) {
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    StatefulView<List<PokemonCardUI>>(
        modifier = modifier,
        viewModel = viewModel,
        appBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.pokemon_list_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        content = { data, isLoading ->
            InfiniteScrollList(
                items = data ?: emptyList(),
                isLoading = isLoading,
                isLoadingMore = isLoadingMore,
                hasMore = hasMore,
                onLoadMore = { viewModel.loadMore() },
                modifier = Modifier.fillMaxSize(),
                itemContent = { pokemon, _ -> PokemonCard(pokemon = pokemon) }
            )
        }
    )
}
