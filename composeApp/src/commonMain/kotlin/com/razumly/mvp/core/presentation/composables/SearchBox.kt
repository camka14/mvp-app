package com.razumly.mvp.core.presentation.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.razumly.mvp.core.data.dataTypes.enums.EventType
import com.razumly.mvp.core.data.dataTypes.enums.FieldType
import com.razumly.mvp.core.presentation.util.dateTimeFormat
import com.razumly.mvp.eventSearch.util.EventFilter
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

@Composable
fun SearchBox(
    modifier: Modifier = Modifier,
    placeholder: String,
    filter: Boolean,
    currentFilter: EventFilter? = null,
    onFilterChange: (EventFilter.() -> EventFilter) -> Unit,
    onChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onPositionChange: (Offset, IntSize) -> Unit
) {
    var searchInput by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var showFilterDropdown by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
        val position = coordinates.positionInRoot()
        val size = coordinates.size
        onPositionChange(position, size)
    }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(value = searchInput,
                onValueChange = { newQuery ->
                    searchInput = newQuery
                    onChange(newQuery)
                },
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search, contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchInput.isNotEmpty()) {
                        IconButton(onClick = {
                            searchInput = ""
                            onChange("")
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear, contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f).onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    onFocusChange(focusState.isFocused)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch(searchInput)
                    focusManager.clearFocus()
                })
            )

            if (filter && currentFilter != null) {
                Spacer(modifier = Modifier.width(8.dp))

                // Filter Button with Badge
                Box {
                    IconButton(onClick = { showFilterDropdown = !showFilterDropdown }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Filter",
                            tint = if (isFilterActive(currentFilter)) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Active filter indicator
                    if (isFilterActive(currentFilter)) {
                        Box(
                            modifier = Modifier.size(8.dp).background(
                                MaterialTheme.colorScheme.primary, CircleShape
                            ).align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }

        // Filter Dropdown
        if (filter && currentFilter != null) {
            FilterDropdown(visible = showFilterDropdown,
                currentFilter = currentFilter,
                onFilterChange = onFilterChange,
                onDismiss = { showFilterDropdown = false })
        }
    }
}

private fun isFilterActive(filter: EventFilter): Boolean {
    return filter.eventType != null || filter.field != null || filter.price != null || filter.date != null
}

@Composable
private fun FilterDropdown(
    visible: Boolean,
    currentFilter: EventFilter,
    onFilterChange: (EventFilter.() -> EventFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Events",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(onClick = {
                            onFilterChange { EventFilter() }
                            onDismiss()
                        }) {
                            Text("Clear All")
                        }
                    }

                    HorizontalDivider()

                    // Event Type Filter
                    EventTypeFilterSection(
                        currentFilter = currentFilter, onFilterChange = onFilterChange
                    )

                    HorizontalDivider()

                    // Field Type Filter
                    FieldTypeFilterSection(
                        currentFilter = currentFilter, onFilterChange = onFilterChange
                    )

                    HorizontalDivider()

                    // Price Filter
                    PriceFilterSection(
                        currentFilter = currentFilter, onFilterChange = onFilterChange
                    )

                    HorizontalDivider()

                    // Date Filter
                    DateFilterSection(
                        currentFilter = currentFilter,
                        { showStartPicker = true },
                        { showEndPicker = true }
                    )

                    // Apply Button
                    Button(
                        onClick = onDismiss, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Filters")
                    }
                }
                PlatformDateTimePicker(
                    onDateSelected = { selectedInstant ->
                        onFilterChange {
                            copy(
                                date = (selectedInstant ?: Clock.System.now()) to date.second
                            )
                        }
                        showStartPicker = false
                    },
                    onDismissRequest = { showStartPicker = false },
                    showPicker = showStartPicker,
                    getTime = false,
                    canSelectPast = true,
                )
                PlatformDateTimePicker(
                    onDateSelected = { selectedInstant ->
                        onFilterChange {
                            copy(
                                date = date.first to selectedInstant
                            )
                        }
                        showEndPicker = false
                    },
                    onDismissRequest = { showEndPicker = false },
                    showPicker = showEndPicker,
                    getTime = false,
                    canSelectPast = true,
                )
            }
        }
    }
}

@Composable
private fun EventTypeFilterSection(
    currentFilter: EventFilter, onFilterChange: (EventFilter.() -> EventFilter) -> Unit
) {
    Column {
        Text(
            text = "Event Type",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            item {
                FilterChip(selected = currentFilter.eventType == null,
                    onClick = { onFilterChange { copy(eventType = null) } },
                    label = { Text("All") })
            }

            item {
                FilterChip(selected = currentFilter.eventType == EventType.TOURNAMENT,
                    onClick = { onFilterChange { copy(eventType = EventType.TOURNAMENT) } },
                    label = { Text("Tournaments") })
            }

            item {
                FilterChip(selected = currentFilter.eventType == EventType.EVENT,
                    onClick = { onFilterChange { copy(eventType = EventType.EVENT) } },
                    label = { Text("Events") })
            }
        }
    }
}

@Composable
private fun FieldTypeFilterSection(
    currentFilter: EventFilter, onFilterChange: (EventFilter.() -> EventFilter) -> Unit
) {
    Column {
        Text(
            text = "Field Type",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            item {
                FilterChip(selected = currentFilter.field == null,
                    onClick = { onFilterChange { copy(field = null) } },
                    label = { Text("All Fields") })
            }

            items(FieldType.entries) { fieldType ->
                FilterChip(selected = currentFilter.field == fieldType,
                    onClick = { onFilterChange { copy(field = fieldType) } },
                    label = { Text(fieldType.name) })
            }
        }
    }
}

@Composable
private fun PriceFilterSection(
    currentFilter: EventFilter, onFilterChange: (EventFilter.() -> EventFilter) -> Unit
) {
    var enablePriceFilter by remember(currentFilter.price) {
        mutableStateOf(currentFilter.price != null)
    }
    var priceRange by remember(currentFilter.price) {
        mutableStateOf(currentFilter.price ?: (0.0 to 100.0))
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price Range",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(checked = enablePriceFilter, onCheckedChange = { enabled ->
                enablePriceFilter = enabled
                onFilterChange {
                    copy(price = if (enabled) priceRange else null)
                }
            })
        }

        if (enablePriceFilter) {
            Text(
                text = "$${priceRange.first.toInt()} - $${priceRange.second.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            RangeSlider(
                value = priceRange.first.toFloat()..priceRange.second.toFloat(),
                onValueChange = { range ->
                    priceRange = range.start.toDouble() to range.endInclusive.toDouble()
                    onFilterChange { copy(price = priceRange) }
                },
                valueRange = 0f..200f,
                steps = 19,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun DateFilterSection(
    currentFilter: EventFilter, onStartDateClicked: () -> Unit, onEndDateClicked: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Date Range",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentFilter.date.first.toLocalDateTime(
                    TimeZone.currentSystemDefault()
                ).format(dateTimeFormat),
                onValueChange = {},
                label = { Text("Start Date & Time") },
                modifier = Modifier.weight(1f)
                    .clickable(onClick = onStartDateClicked),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = OutlinedTextFieldDefaults.colors().focusedContainerColor,
                    disabledTextColor = OutlinedTextFieldDefaults.colors().focusedTextColor,
                    disabledLabelColor = OutlinedTextFieldDefaults.colors().focusedLabelColor,
                    disabledBorderColor = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
                )
            )
            OutlinedTextField(
                value = currentFilter.date.second?.toLocalDateTime(
                    TimeZone.currentSystemDefault()
                )?.format(dateTimeFormat) ?: "Select an End Date",
                onValueChange = {},
                label = { Text("End Date") },
                modifier = Modifier.weight(1f)
                    .clickable(onClick = onEndDateClicked),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = OutlinedTextFieldDefaults.colors().focusedContainerColor,
                    disabledTextColor = OutlinedTextFieldDefaults.colors().focusedTextColor,
                    disabledLabelColor = OutlinedTextFieldDefaults.colors().focusedLabelColor,
                    disabledBorderColor = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
                )
            )
        }
    }
}
