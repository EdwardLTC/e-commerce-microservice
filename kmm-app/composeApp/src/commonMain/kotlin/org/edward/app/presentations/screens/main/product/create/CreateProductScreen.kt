package org.edward.app.presentations.screens.main.product.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.edward.app.presentations.screens.components.authOutlinedFieldColors

class CreateProductScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<CreateProductScreenModel>()
        val state by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                // Top bar
                TopSection(
                    currentStep = state.currentStep,
                    onBack = {
                        if (state.currentStep == 0) navigator.pop() else screenModel.goBack()
                    },
                )

                // Step title
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.stepTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        state.stepSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Step content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = state.currentStep,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            (slideInHorizontally { it * direction } + fadeIn())
                                .togetherWith(slideOutHorizontally { -it * direction } + fadeOut())
                        },
                    ) { step ->
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                        ) {
                            when (step) {
                                0 -> StepProductInfo(state, screenModel)
                                1 -> StepOptionTypes(state, screenModel)
                                2 -> StepOptionValues(state, screenModel)
                                3 -> StepVariants(state, screenModel)
                            }
                            Spacer(Modifier.height(100.dp))
                        }
                    }
                }
            }

            // Error message
            if (state.error != null) {
                Text(
                    state.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }

            // Bottom action bar
            BottomActionBar(
                state = state,
                onNext = { screenModel.goNext { navigator.pop() } },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    // ── Top bar with back + step indicator ───────────────────────────────

    @Composable
    private fun TopSection(currentStep: Int, onBack: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 24.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(8.dp))

            // Step dots
            repeat(CreateProductScreenModel.TOTAL_STEPS) { i ->
                val isActive = i <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }

    // ── Step 0: Product Info ─────────────────────────────────────────────

    @Composable
    private fun StepProductInfo(
        state: CreateProductScreenModel.UiState,
        sm: CreateProductScreenModel,
    ) {
        val fieldColors = authOutlinedFieldColors()

        OutlinedTextField(
            value = state.name,
            onValueChange = sm::onNameChange,
            label = { Text("Product name *") },
            placeholder = { Text("e.g. AIRism Cotton T-Shirt") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            enabled = !state.isSubmitting,
            colors = fieldColors,
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = state.brand,
            onValueChange = sm::onBrandChange,
            label = { Text("Brand *") },
            placeholder = { Text("e.g. Essentials") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            enabled = !state.isSubmitting,
            colors = fieldColors,
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = sm::onDescriptionChange,
            label = { Text("Description") },
            placeholder = { Text("Product details...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = MaterialTheme.shapes.small,
            enabled = !state.isSubmitting,
            colors = fieldColors,
        )
        Spacer(Modifier.height(20.dp))

        // Image URLs
        Text(
            "IMAGES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2,
        )
        Spacer(Modifier.height(8.dp))

        state.imageUrls.forEachIndexed { i, url ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    url,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(
                    onClick = { sm.removeImageUrl(i) },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.imageUrl,
                onValueChange = sm::onImageUrlChange,
                placeholder = { Text("Paste image URL") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                enabled = !state.isSubmitting,
                colors = fieldColors,
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = sm::addImageUrl,
                modifier = Modifier.size(40.dp),
                enabled = state.imageUrl.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
            }
        }
    }

    // ── Step 1: Option Types ─────────────────────────────────────────────

    @Composable
    private fun StepOptionTypes(
        state: CreateProductScreenModel.UiState,
        sm: CreateProductScreenModel,
    ) {
        if (state.optionTypes.isEmpty()) {
            EmptyHint("No option types yet. Add attributes like Size, Color, or Material.")
        }

        state.optionTypes.forEachIndexed { i, ot ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ot.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    if (ot.serverId != null) {
                        Text(
                            "Saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                if (ot.serverId == null) {
                    IconButton(
                        onClick = { sm.removeOptionType(i) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.newOptionTypeName,
                onValueChange = sm::onNewOptionTypeNameChange,
                placeholder = { Text("e.g. Size") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                enabled = !state.isSubmitting,
                colors = authOutlinedFieldColors(),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = sm::addOptionType,
                modifier = Modifier.size(40.dp),
                enabled = state.newOptionTypeName.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
            }
        }
    }

    // ── Step 2: Option Values ────────────────────────────────────────────

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun StepOptionValues(
        state: CreateProductScreenModel.UiState,
        sm: CreateProductScreenModel,
    ) {
        if (state.optionTypes.isEmpty()) {
            EmptyHint("You didn't add any option types. You can skip this step.")
            return
        }

        state.optionTypes.forEachIndexed { ti, ot ->
            Text(
                ot.name.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2,
            )
            Spacer(Modifier.height(10.dp))

            if (ot.values.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ot.values.forEachIndexed { vi, ov ->
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraSmall)
                                .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(ov.value, style = MaterialTheme.typography.bodySmall)
                            IconButton(
                                onClick = { sm.removeOptionValue(ti, vi) },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.newOptionValueInputs[ti] ?: "",
                    onValueChange = { sm.onNewOptionValueChange(ti, it) },
                    placeholder = { Text("Add value...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    enabled = !state.isSubmitting,
                    colors = authOutlinedFieldColors(),
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { sm.addOptionValue(ti) },
                    modifier = Modifier.size(40.dp),
                    enabled = (state.newOptionValueInputs[ti] ?: "").isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
                }
            }

            if (ti < state.optionTypes.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                )
            }
        }
    }

    // ── Step 3: Variants ─────────────────────────────────────────────────

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun StepVariants(
        state: CreateProductScreenModel.UiState,
        sm: CreateProductScreenModel,
    ) {
        val fieldColors = authOutlinedFieldColors()
        val hasOptions = state.optionTypes.any { ot -> ot.values.any { it.serverId != null } }

        state.variants.forEachIndexed { i, variant ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Variant ${i + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(
                        onClick = { sm.removeVariant(i) },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = variant.sku,
                    onValueChange = { sm.onVariantSkuChange(i, it) },
                    label = { Text("SKU *") },
                    placeholder = { Text("e.g. SHIRT-BLK-M") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    enabled = !state.isSubmitting,
                    colors = fieldColors,
                )
                Spacer(Modifier.height(10.dp))

                Row {
                    OutlinedTextField(
                        value = variant.price,
                        onValueChange = { sm.onVariantPriceChange(i, it) },
                        label = { Text("Price *") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !state.isSubmitting,
                        colors = fieldColors,
                    )
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = variant.stock,
                        onValueChange = { sm.onVariantStockChange(i, it) },
                        label = { Text("Stock *") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !state.isSubmitting,
                        colors = fieldColors,
                    )
                }

                // Option selection grouped by option type
                if (hasOptions) {
                    state.optionTypes.forEachIndexed { ti, ot ->
                        val values = ot.values.filter { it.serverId != null }
                        if (values.isEmpty()) return@forEachIndexed

                        Spacer(Modifier.height(14.dp))
                        Text(
                            ot.name.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2,
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            values.forEach { ov ->
                                val valueId = ov.serverId ?: return@forEach
                                val isSelected = variant.selectedOptionValueIds.contains(valueId)
                                Box(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .then(
                                            if (isSelected) Modifier.background(MaterialTheme.colorScheme.onSurface)
                                            else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraSmall)
                                        )
                                        .clickable { sm.selectVariantOption(i, ti, valueId) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        ov.value,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = sm::addVariant,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            enabled = !state.isSubmitting,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Variant")
        }
    }

    // ── Bottom action bar ────────────────────────────────────────────────

    @Composable
    private fun BottomActionBar(
        state: CreateProductScreenModel.UiState,
        onNext: () -> Unit,
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
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = state.canProceed && !state.isSubmitting,
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.surface,
                        )
                    } else {
                        val label = if (state.currentStep == CreateProductScreenModel.TOTAL_STEPS - 1) "CREATE PRODUCT" else "NEXT"
                        Text(label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    @Composable
    private fun EmptyHint(message: String) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp),
        )
    }
}
