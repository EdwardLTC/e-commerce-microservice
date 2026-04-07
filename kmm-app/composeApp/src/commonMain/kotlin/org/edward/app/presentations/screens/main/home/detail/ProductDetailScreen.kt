package org.edward.app.presentations.screens.main.home.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.edward.app.data.remote.product.ProductDetail
import org.edward.app.data.remote.product.Variant
import org.edward.app.data.utils.formatPrice
import org.edward.app.data.utils.formatRating

class ProductDetailScreen(private val productId: String) : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ProductDetailScreenModel>()
        val state by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(productId) {
            screenModel.loadProduct(productId)
        }

        when {
            state.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { screenModel.loadProduct(productId) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            state.product != null -> {
                Box(Modifier.fillMaxSize()) {
                    ProductDetailBody(
                        product = state.product!!,
                        selectedOptions = state.selectedOptions,
                        selectedVariant = state.selectedVariant,
                        quantity = state.quantity,
                        onOptionSelected = screenModel::selectOption,
                        isOptionValueAvailable = screenModel::isOptionValueAvailable,
                        onQuantityChange = screenModel::updateQuantity,
                    )

                    // Floating back button
                    IconButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(start = 8.dp, top = 4.dp)
                            .align(Alignment.TopStart)
                            .size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        ),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    // Sticky bottom Add to Cart bar
                    AddToCartBar(
                        selectedVariant = state.selectedVariant,
                        addedToCart = state.addedToCart,
                        onAddToCart = screenModel::addToCart,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ProductDetailBody(
        product: ProductDetail,
        selectedOptions: Map<String, String>,
        selectedVariant: Variant?,
        quantity: Int,
        onOptionSelected: (String, String) -> Unit,
        isOptionValueAvailable: (String, String) -> Boolean,
        onQuantityChange: (Int) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Edge-to-edge image carousel
            if (product.mediaUrls.isNotEmpty()) {
                ImageCarousel(product.mediaUrls)
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                // Brand
                if (product.brand.isNotEmpty()) {
                    Text(
                        product.brand.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2,
                    )
                    Spacer(Modifier.height(6.dp))
                }

                // Product name
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                // Price
                if (selectedVariant != null) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$${selectedVariant.effectivePrice.formatPrice()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (selectedVariant.hasDiscount) {
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "$${selectedVariant.price.formatPrice()}",
                                style = MaterialTheme.typography.bodyLarge,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Rating & sales
                if (product.averageRating > 0 || product.totalSaleCount > 0) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (product.averageRating > 0) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFB300)
                            )
                            Text(
                                " ${product.averageRating.formatRating()}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (product.totalSaleCount > 0) {
                            if (product.averageRating > 0) {
                                Text(
                                    "  ·  ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                            Text(
                                "${product.totalSaleCount} sold",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Stock
                if (selectedVariant != null) {
                    Text(
                        if (selectedVariant.isAvailable) "In stock" else "Out of stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedVariant.isAvailable) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                )

                // Color / Size selection
                if (product.variants.size > 1) {
                    product.optionTypes.forEach { optType ->
                        Text(
                            optType.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            optType.optionValues.forEach { optVal ->
                                val isSelected = selectedOptions[optType.id] == optVal.id
                                val isAvailable = isOptionValueAvailable(optType.id, optVal.id)

                                val borderColor by animateColorAsState(
                                    targetValue = when {
                                        isSelected -> MaterialTheme.colorScheme.onSurface
                                        !isAvailable -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    },
                                    animationSpec = tween(150),
                                    label = "optBorder",
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .border(
                                            if (isSelected) 1.5.dp else 1.dp,
                                            borderColor,
                                            MaterialTheme.shapes.extraSmall,
                                        )
                                        .clickable(enabled = isAvailable) {
                                            onOptionSelected(optType.id, optVal.id)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        optVal.value,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            !isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            isSelected -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Quantity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onQuantityChange(quantity - 1) },
                                enabled = quantity > 1,
                                modifier = Modifier.size(38.dp),
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                "$quantity",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            IconButton(
                                onClick = { onQuantityChange(quantity + 1) },
                                modifier = Modifier.size(38.dp),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                )

                // Description
                if (product.description.isNotEmpty()) {
                    Text(
                        "DETAILS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.15,
                    )
                }

                // Bottom spacer to clear the sticky Add to Cart bar
                Spacer(Modifier.height(100.dp))
            }
        }
    }

    @Composable
    private fun AddToCartBar(
        selectedVariant: Variant?,
        addedToCart: Boolean,
        onAddToCart: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = selectedVariant != null && selectedVariant.isAvailable && !addedToCart,
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = if (addedToCart)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = if (addedToCart)
                            MaterialTheme.colorScheme.onTertiary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    if (addedToCart) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ADDED TO CART", style = MaterialTheme.typography.labelLarge)
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ADD TO CART", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    @Composable
    private fun ImageCarousel(urls: List<String>) {
        val pagerState = rememberPagerState(pageCount = { urls.size })

        Box {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
            ) { page ->
                KamelImage(
                    resource = { asyncPainterResource(urls[page]) },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            if (urls.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    repeat(urls.size) { index ->
                        val isSelected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }
    }
}
