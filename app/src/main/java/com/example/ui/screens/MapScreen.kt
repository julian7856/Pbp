package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CityLocation
import com.example.ui.components.HappyCommunityHeaderBanner
import com.example.ui.components.InteractiveMapView
import com.example.ui.viewmodel.MapUiState

@Composable
fun MapScreen(
    mapState: MapUiState,
    isCelsius: Boolean,
    currentTimeMs: Long,
    onMapTapped: (Double, Double) -> Unit,
    onCenterChanged: (Double, Double, Float?) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCitySelected: (CityLocation) -> Unit,
    onCurrentLocationRequested: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("map_screen")
    ) {
        // Full bleed Slippy Map
        InteractiveMapView(
            centerLat = mapState.centerLat,
            centerLon = mapState.centerLon,
            zoomLevel = mapState.zoomLevel,
            selectedLocation = mapState.selectedLocation,
            weatherPreview = mapState.weatherPreview,
            isLoadingPreview = mapState.isLoadingPreview,
            isCelsius = isCelsius,
            currentTimeMs = currentTimeMs,
            onMapTapped = onMapTapped,
            onCenterChanged = onCenterChanged,
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            onCitySelected = onCitySelected,
            onCurrentLocationRequested = onCurrentLocationRequested
        )

        // Top Overlay: HappyCommunity Header Banner
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 40.dp) // allow clearance below chips
        ) {
            HappyCommunityHeaderBanner()
        }

        // Floating Search Button on bottom-start
        FloatingActionButton(
            onClick = onOpenSearch,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 100.dp)
                .size(48.dp)
                .testTag("map_search_fab"),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = androidx.compose.ui.graphics.Color.White
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Location")
        }
    }
}
