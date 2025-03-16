package com.razumly.mvp.core.presentation.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.razumly.mvp.core.data.dataTypes.EventAbs
import com.razumly.mvp.core.presentation.util.cleanup
import com.razumly.mvp.core.presentation.util.dateFormat
import com.razumly.mvp.core.presentation.util.toTitleCase
import com.razumly.mvp.eventSearch.composables.StylizedText
import com.razumly.mvp.eventSearch.util.TextPatterns
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalHazeMaterialsApi::class,
    ExperimentalHazeApi::class
)
@Composable
fun EventDetails(
    event: EventAbs,
    expanded: Boolean,
    onFavoriteClick: () -> Unit,
    favoritesModifier: Modifier,
    onMapClick: (Offset) -> Unit,
    joinButton: @Composable () -> Unit
) {
    var imageStateText by remember { mutableStateOf("initial") }
    val patterns = TextPatterns(event.name)
    val hazeState = remember { HazeState() }
    var mapButtonOffset by remember { mutableStateOf(Offset.Zero) }

    val dateRangeText = remember(event.start, event.end) {
        val startDate = event.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val endDate = event.end.toLocalDateTime(TimeZone.currentSystemDefault()).date

        val startStr = startDate.format(dateFormat)

        if (startDate != endDate) {
            val endStr = endDate.format(dateFormat)
            "$startStr - $endStr"
        } else {
            startStr
        }
    }
    Box {
        AsyncImage(
            model = event.imageUrl,
            contentDescription = "Event Image",
            modifier = Modifier
                .matchParentSize()
                .hazeSource(hazeState),
            contentScale = ContentScale.Crop,
            onState = { state ->
                imageStateText = when (state) {
                    is AsyncImagePainter.State.Loading -> "Loading"
                    is AsyncImagePainter.State.Error ->
                        "Error loading image: ${state.result.throwable}"

                    is AsyncImagePainter.State.Success -> "success"
                    is AsyncImagePainter.State.Empty -> "Image is Empty"
                }
            }
        )
        if (imageStateText != "success") {
            Text(imageStateText)
        }
        IconButton(
            onClick = onFavoriteClick,
            modifier = favoritesModifier
                .align(Alignment.TopEnd)
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Default.AddCircle),
                contentDescription = "Favorite",
            )
        }

        // Event Details Section
        Column {
            Spacer(modifier = Modifier.height(200.dp))
            Column(
                modifier = Modifier
                    .hazeEffect(
                    hazeState,
                    HazeMaterials.ultraThin(MaterialTheme.colorScheme.onBackground)
                    ) {
                        inputScale = HazeInputScale.Fixed(0.5f)
                        progressive = HazeProgressive.verticalGradient(
                            easing = FastOutSlowInEasing,
                            startIntensity = 0f,
                            endIntensity = 1f
                        )
                    }
                    .padding(16.dp)
                    .wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                AnimatedVisibility(
                    expanded,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Button(
                            modifier = Modifier
                                .onGloballyPositioned { layoutCoordinates ->
                                    val boundsInWindow = layoutCoordinates.boundsInWindow()
                                    mapButtonOffset = boundsInWindow.center
                                },
                            onClick = { onMapClick(mapButtonOffset) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text("View on Map")
                            Icon(Icons.Default.Place, contentDescription = "View on Map Button")
                        }
                        joinButton()
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background
                    )
                    Text(
                        text = event.rating.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.LocationOn),
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.background
                    )
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                }

                AnimatedVisibility(expanded) {
                    StylizedText(event.description, patterns)
                }

                StylizedText("${event.fieldType} ${event.eventType}".toTitleCase(), patterns)
                StylizedText("Divisions: ${event.divisions.joinToString(", ")}", patterns)
                androidx.compose.material3.HorizontalDivider(thickness = 2.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateRangeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                    Text(
                        text = "$" + cleanup("${event.price}"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}