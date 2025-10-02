package com.mdymnoff.pokemonium.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mdymnoff.pokemonium.R

/**
 * A reusable component that supports infinite scrolling behavior.
 * 
 * @param items The list of items to display
 * @param isLoading Whether the initial loading is in progress
 * @param isLoadingMore Whether more items are being loaded
 * @param hasMore Whether there are more items to load
 * @param onLoadMore Callback triggered when more items should be loaded
 * @param modifier Modifier for the component
 * @param listState LazyListState for controlling the list
 * @param loadMoreThreshold How many items from the end should trigger loading more items
 * @param itemContent Composable function to render each item
 * @param loadingContent Optional composable for initial loading state
 * @param loadingMoreContent Optional composable for loading more state
 * @param emptyContent Optional composable for empty state
 * @param errorContent Optional composable for error state
 */
@Composable
fun <T> InfiniteScrollList(
    items: List<T>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    loadMoreThreshold: Int = 3,
    itemContent: @Composable (item: T, index: Int) -> Unit,
    loadingContent: @Composable (() -> Unit)? = null,
    loadingMoreContent: @Composable (() -> Unit)? = null,
    emptyContent: @Composable (() -> Unit)? = null,
    errorContent: @Composable (() -> Unit)? = null
) {
    LaunchedEffect(listState, hasMore, isLoadingMore, isLoading) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

            totalItemsNumber > 0 && lastVisibleItemIndex >= (totalItemsNumber - loadMoreThreshold)
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && hasMore && !isLoadingMore && !isLoading && items.isNotEmpty()) {
                onLoadMore()
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && items.isEmpty() -> { loadingContent?.invoke() ?: DefaultLoadingContent() }
            items.isEmpty() -> { emptyContent?.invoke() ?: DefaultEmptyContent() }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = items,
                        key = { index, item -> "$index-$item" }
                    ) { index, item ->
                        itemContent(item, index)
                    }

                    if (isLoadingMore) {
                        item { loadingMoreContent?.invoke() ?: DefaultLoadingMoreContent() }
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.loading_text),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun DefaultLoadingMoreContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Text(
                text = stringResource(R.string.loading_more_text),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DefaultEmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_items_found_text),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
