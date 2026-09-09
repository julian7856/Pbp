package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CityLocation
import com.example.data.model.ClockZone
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WeatherApiResponse
import com.example.data.model.defaultCities
import com.example.data.model.defaultWorldClocks
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: WeatherApiResponse? = null,
    val hourly: List<HourlyForecastItem> = emptyList(),
    val daily: List<DailyForecastItem> = emptyList(),
    val errorMessage: String? = null
)

data class MapUiState(
    val centerLat: Double = 51.5074,
    val centerLon: Double = -0.1278,
    val zoomLevel: Float = 11f,
    val selectedLocation: CityLocation? = null,
    val weatherPreview: WeatherApiResponse? = null,
    val isLoadingPreview: Boolean = false
)

class MainViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isCelsius = MutableStateFlow(true)
    val isCelsius: StateFlow<Boolean> = _isCelsius.asStateFlow()

    private val _currentCity = MutableStateFlow(defaultCities[1]) // London default
    val currentCity: StateFlow<CityLocation> = _currentCity.asStateFlow()

    private val _savedCities = MutableStateFlow(defaultCities)
    val savedCities: StateFlow<List<CityLocation>> = _savedCities.asStateFlow()

    private val _weatherState = MutableStateFlow(WeatherUiState())
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _worldClocks = MutableStateFlow(defaultWorldClocks)
    val worldClocks: StateFlow<List<ClockZone>> = _worldClocks.asStateFlow()

    private val _mapState = MutableStateFlow(
        MapUiState(
            centerLat = defaultCities[1].latitude,
            centerLon = defaultCities[1].longitude,
            selectedLocation = defaultCities[1]
        )
    )
    val mapState: StateFlow<MapUiState> = _mapState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CityLocation>>(emptyList())
    val searchResults: StateFlow<List<CityLocation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Start real-time clock ticking
        viewModelScope.launch {
            while (isActive) {
                _currentTimeMs.value = System.currentTimeMillis()
                delay(500)
            }
        }
        // Load initial weather
        fetchWeatherForCity(_currentCity.value)
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleTemperatureUnit() {
        _isCelsius.update { !it }
    }

    fun selectCity(city: CityLocation) {
        _currentCity.value = city
        fetchWeatherForCity(city)
        _mapState.update {
            it.copy(
                centerLat = city.latitude,
                centerLon = city.longitude,
                selectedLocation = city
            )
        }
    }

    fun fetchWeatherForCity(city: CityLocation) {
        viewModelScope.launch {
            _weatherState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getForecast(city.latitude, city.longitude)
            result.onSuccess { response ->
                val hourly = repository.parseHourlyForecast(response)
                val daily = repository.parseDailyForecast(response)
                _weatherState.update {
                    it.copy(
                        isLoading = false,
                        weather = response,
                        hourly = hourly,
                        daily = daily,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _weatherState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to fetch weather"
                    )
                }
            }
        }
    }

    fun onLocationReceived(lat: Double, lon: Double) {
        val customLoc = CityLocation(
            name = "Current Location",
            country = String.format(Locale.US, "%.3f°, %.3f°", lat, lon),
            latitude = lat,
            longitude = lon
        )
        selectCity(customLoc)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(350) // debounce
            val results = repository.searchCities(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun addWorldClock(zone: ClockZone) {
        if (_worldClocks.value.none { it.id == zone.id || (it.cityName == zone.cityName && it.timeZoneId == zone.timeZoneId) }) {
            _worldClocks.update { it + zone }
        }
    }

    fun removeWorldClock(id: String) {
        _worldClocks.update { list -> list.filterNot { it.id == id } }
    }

    fun onMapTapped(lat: Double, lon: Double) {
        val tappedLoc = CityLocation(
            name = String.format(Locale.US, "Point (%.3f, %.3f)", lat, lon),
            country = "Pinned Location",
            latitude = lat,
            longitude = lon
        )
        _mapState.update {
            it.copy(
                centerLat = lat,
                centerLon = lon,
                selectedLocation = tappedLoc,
                isLoadingPreview = true
            )
        }
        viewModelScope.launch {
            val result = repository.getForecast(lat, lon)
            result.onSuccess { response ->
                _mapState.update {
                    it.copy(
                        weatherPreview = response,
                        isLoadingPreview = false
                    )
                }
            }.onFailure {
                _mapState.update { it.copy(isLoadingPreview = false) }
            }
        }
    }

    fun updateMapCenter(lat: Double, lon: Double, zoom: Float? = null) {
        _mapState.update {
            it.copy(
                centerLat = lat.coerceIn(-85.0, 85.0),
                centerLon = ((lon + 180.0) % 360.0 + 360.0) % 360.0 - 180.0,
                zoomLevel = zoom ?: it.zoomLevel
            )
        }
    }

    fun zoomIn() {
        _mapState.update { it.copy(zoomLevel = (it.zoomLevel + 1f).coerceAtMost(18f)) }
    }

    fun zoomOut() {
        _mapState.update { it.copy(zoomLevel = (it.zoomLevel - 1f).coerceAtLeast(2f)) }
    }
}
