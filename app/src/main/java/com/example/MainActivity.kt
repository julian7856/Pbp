package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClockZone
import com.example.ui.components.CitySearchDialog
import com.example.ui.screens.ClockScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isCelsius by viewModel.isCelsius.collectAsStateWithLifecycle()
    val currentCity by viewModel.currentCity.collectAsStateWithLifecycle()
    val savedCities by viewModel.savedCities.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    val worldClocks by viewModel.worldClocks.collectAsStateWithLifecycle()
    val mapState by viewModel.mapState.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var showSearchDialog by remember { mutableStateOf(false) }
    var isAddingClockMode by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            fetchDeviceLocation(fusedLocationClient, viewModel, snackbarHostState, scope)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Location permission denied.")
            }
        }
    }

    val onLocationRequested: () -> Unit = {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            fetchDeviceLocation(fusedLocationClient, viewModel, snackbarHostState, scope)
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Maps Clock Weather",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Made by HappyCommunity",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isAddingClockMode = false
                            showSearchDialog = true
                        },
                        modifier = Modifier.testTag("app_bar_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Locations"
                        )
                    }

                    IconButton(
                        onClick = onLocationRequested,
                        modifier = Modifier.testTag("app_bar_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My Location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Cloud else Icons.Outlined.Cloud,
                            contentDescription = "Weather"
                        )
                    },
                    label = { Text("Weather") },
                    modifier = Modifier.testTag("nav_weather_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.Schedule else Icons.Outlined.Schedule,
                            contentDescription = "Clock"
                        )
                    },
                    label = { Text("Clock") },
                    modifier = Modifier.testTag("nav_clock_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.Map else Icons.Outlined.Map,
                            contentDescription = "Maps"
                        )
                    },
                    label = { Text("Maps") },
                    modifier = Modifier.testTag("nav_maps_tab")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "ScreenTransition") { tab ->
                when (tab) {
                    0 -> WeatherScreen(
                        currentCity = currentCity,
                        savedCities = savedCities,
                        weatherState = weatherState,
                        isCelsius = isCelsius,
                        onCitySelected = { viewModel.selectCity(it) },
                        onToggleUnit = { viewModel.toggleTemperatureUnit() },
                        onRefresh = { viewModel.fetchWeatherForCity(currentCity) },
                        onOpenSearch = {
                            isAddingClockMode = false
                            showSearchDialog = true
                        }
                    )
                    1 -> ClockScreen(
                        currentCity = currentCity,
                        worldClocks = worldClocks,
                        currentTimeMs = currentTimeMs,
                        onAddClockRequested = {
                            isAddingClockMode = true
                            showSearchDialog = true
                        },
                        onRemoveClock = { id -> viewModel.removeWorldClock(id) }
                    )
                    2 -> MapScreen(
                        mapState = mapState,
                        isCelsius = isCelsius,
                        currentTimeMs = currentTimeMs,
                        onMapTapped = { lat, lon -> viewModel.onMapTapped(lat, lon) },
                        onCenterChanged = { lat, lon, zoom -> viewModel.updateMapCenter(lat, lon, zoom) },
                        onZoomIn = { viewModel.zoomIn() },
                        onZoomOut = { viewModel.zoomOut() },
                        onCitySelected = { viewModel.selectCity(it) },
                        onCurrentLocationRequested = onLocationRequested,
                        onOpenSearch = {
                            isAddingClockMode = false
                            showSearchDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showSearchDialog) {
        CitySearchDialog(
            searchQuery = searchQuery,
            searchResults = searchResults,
            isSearching = isSearching,
            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onCitySelected = { city ->
                if (isAddingClockMode) {
                    viewModel.addWorldClock(
                        ClockZone(
                            id = "clock_${System.currentTimeMillis()}",
                            cityName = city.name,
                            countryName = city.country,
                            timeZoneId = city.timezone
                        )
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("Added ${city.name} to World Clock")
                    }
                } else {
                    viewModel.selectCity(city)
                }
            },
            onDismiss = { showSearchDialog = false }
        )
    }
}

@SuppressLint("MissingPermission")
private fun fetchDeviceLocation(
    client: FusedLocationProviderClient,
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    try {
        client.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                viewModel.onLocationReceived(loc.latitude, loc.longitude)
                scope.launch {
                    snackbarHostState.showSnackbar("Updated location coordinates")
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("GPS coordinate unavailable, using default city")
                }
            }
        }.addOnFailureListener {
            scope.launch {
                snackbarHostState.showSnackbar("Could not obtain location")
            }
        }
    } catch (_: SecurityException) {
        // Handled
    }
}
