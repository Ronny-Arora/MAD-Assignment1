/* Class File: CatalogViewModel.kt
 *  - Manages app state (grid/list, category, query, favourites).
 *  - "Near me": userLocation, distance sort (nearest/furthest), nearby-only radius.
 *  - Live updates when runtime coordinates are resolved.
 *
 *  Date created: 29/08/2025
 *  Last modified: 14/09/2025
 */
package com.example.assignment1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CatalogViewModel : ViewModel() {

    // --- Types ---
    data class LatLng(val lat: Double, val lng: Double)
    enum class DistanceSort { NONE, NEAREST, FURTHEST }

    // --- UI state ---
    private val _isGrid = MutableStateFlow(true)
    val isGrid: StateFlow<Boolean> = _isGrid

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    // Favourites (by unique key; using title here)
    private val _favourites = MutableStateFlow<Set<String>>(emptySet())
    val favourites: StateFlow<Set<String>> = _favourites
    // Keep US spelling to avoid breaking any existing calls
    val favorites: StateFlow<Set<String>> = _favourites

    private val _favouritesOnly = MutableStateFlow(false)
    val favouritesOnly: StateFlow<Boolean> = _favouritesOnly

    // --- Source data ---
    private val _allItems = MutableStateFlow(ItemRepository.items)

    // --- Location / near-me state ---
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _distanceSort = MutableStateFlow(DistanceSort.NONE)
    val distanceSort: StateFlow<DistanceSort> = _distanceSort

    private val _nearbyOnly = MutableStateFlow(false)
    val nearbyOnly: StateFlow<Boolean> = _nearbyOnly

    // Runtime-resolved coordinates (e.g., via Places); keyed by title
    private val _resolvedCoords = MutableStateFlow<Map<String, LatLng>>(emptyMap())
    val resolvedCoords: StateFlow<Map<String, LatLng>> = _resolvedCoords

    private val nearbyRadiusKm = 10.0

    // --- Flow grouping to keep combine arity small ---
    private val filters = combine(_selectedCategory, _query) { cat, q -> cat to q }
    private val favState = combine(_favouritesOnly, _favourites) { favOnly, favs -> favOnly to favs }
    private data class DistState(
        val loc: LatLng?,
        val sort: DistanceSort,
        val nearbyOnly: Boolean,
        val resolved: Map<String, LatLng>
    )
    private val distState = combine(_userLocation, _distanceSort, _nearbyOnly, _resolvedCoords) { loc, sort, near, res ->
        DistState(loc, sort, near, res)
    }

    // --- Derived list (reacts to all state incl. runtime-resolved coords) ---
    val items: StateFlow<List<CatalogItem>> =
        combine(_allItems, filters, favState, distState) { list, filterPair, favPair, ds ->
            val (cat, q) = filterPair
            val (favOnly, favs) = favPair
            val (loc, sort, nearbyOnly, resolved) = ds

            // 1) Category
            val afterCat = if (cat == null) list else list.filter { it.category == cat }

            // 2) Text query
            val needle = q.trim().lowercase()
            val afterText = if (needle.isEmpty()) afterCat else afterCat.filter {
                it.title.lowercase().contains(needle) || it.description.lowercase().contains(needle)
            }

            // 3) Favourites
            val afterFav = if (!favOnly) afterText else afterText.filter { favs.contains(it.title) }

            // 4) Distance pairs (item to optional distance)
            val pairs = afterFav.map { item ->
                val d = loc?.let { distanceKm(it, item, resolved) } // null if no coords
                item to d
            }

            // Optional nearby filter
            val filtered = if (nearbyOnly) {
                pairs.filter { (_, d) -> d != null && d <= nearbyRadiusKm }
            } else pairs

            // Optional sort
            val sorted = when (sort) {
                DistanceSort.NEAREST  -> filtered.sortedBy { (_, d) -> d ?: Double.POSITIVE_INFINITY }
                DistanceSort.FURTHEST -> filtered.sortedByDescending { (_, d) -> d ?: Double.NEGATIVE_INFINITY }
                DistanceSort.NONE     -> filtered
            }

            // Unwrap
            sorted.map { it.first }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ItemRepository.items
        )

    // --- Actions (called by UI) ---
    fun setLayout(isGrid: Boolean) { _isGrid.value = isGrid }
    fun toggleLayout() { _isGrid.value = !_isGrid.value }

    fun setCategory(cat: Category?) { _selectedCategory.value = cat }
    fun setQuery(q: String) { _query.value = q }

    fun setFavourites(titles: Set<String>) { _favourites.value = titles }
    fun setFavouritesOnly(on: Boolean) { _favouritesOnly.value = on }
    fun toggleFavouritesOnly() { _favouritesOnly.value = !_favouritesOnly.value }

    fun isFavourite(item: CatalogItem): Boolean = _favourites.value.contains(item.title)
    fun isFavouriteTitle(title: String): Boolean = _favourites.value.contains(title)

    fun toggleFavourite(item: CatalogItem) = toggleFavouriteByTitle(item.title)
    fun toggleFavouriteByTitle(title: String) {
        val cur = _favourites.value
        _favourites.value = if (cur.contains(title)) cur - title else cur + title
    }

    fun setUserLocation(lat: Double, lng: Double) { _userLocation.value = LatLng(lat, lng) }
    fun setDistanceSort(sort: DistanceSort) { _distanceSort.value = sort }
    fun setNearbyOnly(on: Boolean) { _nearbyOnly.value = on }

    /** Store/refresh a resolved coordinate for a given title (triggers live re-sort). */
    fun upsertResolvedCoord(title: String, lat: Double, lng: Double) {
        _resolvedCoords.value = _resolvedCoords.value + (title to LatLng(lat, lng))
    }

    // --- Distance helpers ---
    /** Distance in km from [a] to item's coordinates; falls back to resolved coords if needed. */
    private fun distanceKm(a: LatLng, item: CatalogItem, resolved: Map<String, LatLng>): Double? {
        val coords = when {
            item.lat != null && item.lng != null -> LatLng(item.lat, item.lng)
            else -> resolved[item.title]
        } ?: return null
        return haversineKm(a.lat, a.lng, coords.lat, coords.lng)
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val s1 = sin(dLat / 2)
        val s2 = sin(dLon / 2)
        val a = s1 * s1 + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * s2 * s2
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    /** Convenience for adapter chip/row text. */
    fun distanceKmFor(item: CatalogItem): Double? {
        val loc = _userLocation.value ?: return null
        return distanceKm(loc, item, _resolvedCoords.value)
    }
}
