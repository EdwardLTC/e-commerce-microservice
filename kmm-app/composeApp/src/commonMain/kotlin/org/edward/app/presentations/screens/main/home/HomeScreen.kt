package org.edward.app.presentations.screens.main.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import org.edward.app.data.remote.product.Product
import org.edward.app.data.utils.formatRating
import org.edward.app.presentations.screens.components.EmptyState
import org.edward.app.presentations.screens.main.home.detail.ProductDetailScreen

class HomeScreen : Tab {

    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 0u,
            icon = rememberVectorPainter(Icons.Default.Home),
            title = "Home",
        )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.koinNavigatorScreenModel<HomeScreenModel>()
        val state by screenModel.uiState.collectAsState()

        when {
            state.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.error != null && state.products.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "Could not load catalog",
                        message = state.error ?: "Check your connection and try again."
                    ) {
                        TextButton(
                            onClick = { screenModel.loadProducts() },
                            modifier = Modifier.padding(top = 12.dp)
                        ) { Text("Retry") }
                    }
                }
            }

            state.products.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No products yet",
                        message = "Pull to refresh or try again later."
                    ) {
                        TextButton(
                            onClick = { screenModel.loadProducts() },
                            modifier = Modifier.padding(top = 12.dp)
                        ) { Text("Refresh") }
                    }
                }
            }

            else -> {
                ProductFeed(
                    products = state.products,
                    isLoadingMore = state.isLoadingMore,
                    hasMore = state.hasMore,
                    initialPage = screenModel.lastViewedPage.coerceIn(0, (state.products.size - 1).coerceAtLeast(0)),
                    onProductTap = { product ->
                        navigator.parent?.push(ProductDetailScreen(product.id))
                    },
                    onLoadMore = { screenModel.loadMore() },
                    onPageChanged = { screenModel.saveCurrentPage(it) },
                )
            }
        }
    }

    @Composable
    private fun ProductFeed(
        products: List<Product>,
        isLoadingMore: Boolean,
        hasMore: Boolean,
        initialPage: Int,
        onProductTap: (Product) -> Unit,
        onLoadMore: () -> Unit,
        onPageChanged: (Int) -> Unit,
    ) {
        val pagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { products.size },
        )
        val density = LocalDensity.current
        val navBarInsetDp = with(density) {
            WindowInsets.navigationBars.getBottom(this).toDp()
        }
        val bottomClearance = 56.dp + navBarInsetDp + 16.dp

        val currentPage = pagerState.currentPage

        // Persist current page position
        LaunchedEffect(currentPage) {
            onPageChanged(currentPage)
        }

        // Auto-load more when 5 or fewer items remain
        LaunchedEffect(currentPage, products.size, hasMore) {
            val remaining = products.size - 1 - currentPage
            if (remaining <= 5 && hasMore) {
                onLoadMore()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                ProductPage(
                    product = products[page],
                    isCurrentPage = pagerState.currentPage == page,
                    onTap = { onProductTap(products[page]) },
                    bottomClearance = bottomClearance,
                )
            }

            // Loading indicator at bottom-center when fetching more
            if (isLoadingMore) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bottomClearance + 8.dp)
                        .size(24.dp),
                    strokeWidth = 2.dp,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }

            PageIndicator(
                currentPage = pagerState.currentPage,
                totalPages = products.size,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, bottom = bottomClearance)
            )
        }
    }

    @Composable
    private fun ProductPage(
        product: Product,
        isCurrentPage: Boolean,
        onTap: () -> Unit,
        bottomClearance: Dp,
    ) {
        var showContent by remember { mutableStateOf(false) }

        LaunchedEffect(isCurrentPage) {
            if (isCurrentPage) {
                delay(150)
                showContent = true
            } else {
                showContent = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTap
                )
        ) {
            val imageUrl = product.thumbnailUrl
            if (imageUrl != null) {
                KamelImage(
                    resource = { asyncPainterResource(imageUrl) },
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.88f),
                            ),
                        )
                    )
            )

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400)) + slideInVertically(tween(450)) { it / 5 },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 48.dp, bottom = bottomClearance)
            ) {
                Column {
                    if (product.brand.isNotEmpty()) {
                        Text(
                            product.brand.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f),
                            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2,
                        )
                        Spacer(Modifier.height(6.dp))
                    }

                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        product.displayPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )

                    if (product.rating > 0 || product.totalSaleCount > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (product.rating > 0) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFD54F)
                                )
                                Text(
                                    " ${product.rating.formatRating()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                )
                            }
                            if (product.totalSaleCount > 0) {
                                if (product.rating > 0) {
                                    Text(
                                        "  ·  ",
                                        color = Color.White.copy(alpha = 0.4f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    "${product.totalSaleCount} sold",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PageIndicator(
        currentPage: Int,
        totalPages: Int,
        modifier: Modifier = Modifier,
    ) {
        if (totalPages <= 1) return

        // Sliding window: show at most MAX_DOTS dots, centered on current page
        val maxDots = 7
        val (startIdx, endIdx) = if (totalPages <= maxDots) {
            0 to totalPages - 1
        } else {
            val half = maxDots / 2
            val start = (currentPage - half).coerceIn(0, totalPages - maxDots)
            start to (start + maxDots - 1)
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (index in startIdx..endIdx) {
                val isSelected = index == currentPage
                val distFromEdge = minOf(index - startIdx, endIdx - index)
                val scale = when {
                    isSelected -> 7.dp
                    totalPages <= maxDots -> 4.dp
                    distFromEdge == 0 -> 3.dp
                    else -> 4.dp
                }
                val alpha = when {
                    isSelected -> 1f
                    totalPages <= maxDots -> 0.3f
                    distFromEdge == 0 -> 0.15f
                    distFromEdge == 1 -> 0.25f
                    else -> 0.35f
                }
                Box(
                    modifier = Modifier
                        .padding(vertical = 2.5.dp)
                        .size(scale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = alpha))
                )
            }
        }
    }
}
