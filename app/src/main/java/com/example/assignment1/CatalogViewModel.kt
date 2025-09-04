/* Class File: CatalogViewModel.kt
*       - Manages app state.
*       - Manages grid/list, category, search query (combines into one filtered list).
*
*  Date created: 29/08/2025
*  Last modified: 01/09/2025 */

package com.example.assignment1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

class CatalogViewModel : ViewModel() {

    // --- UI State ---

    // Holds whether the layout is Grid (true) or List (false).
    // Mutable inside ViewModel, but only exposed as read-only to UI.
    private val _isGrid = MutableStateFlow(true)    // Default: Grid View
    val isGrid: StateFlow<Boolean> = _isGrid

    // Holds which category is currently selected by the user.
    // null means "no filter" -> show all times.
    private val _selectedCategory = MutableStateFlow<Category?>(null)   // No filter = show all
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    // Favourites: Store by unique key
    private val _favourites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favourites

    //If true, show only favourite items
    private val _favouritesOnly = MutableStateFlow(false)
    val favouritesOnly: StateFlow<Boolean> = _favouritesOnly


    // --- Source Data (Local in-memory list) ---

    // All catalog items (hard-coded in ItemRepository)
    private val _allItems = MutableStateFlow(ItemRepository.items)

    // --- Apply exposed filtered list ---

    // Filter by category then by text
    val items: StateFlow<List<CatalogItem>> =
        combine(_allItems, _selectedCategory, _query, _favouritesOnly, _favourites) { list, cat, q, favOnly, fav ->
            // 1. Category Filter
            val afterCat = if (cat == null) list else list.filter {it.category == cat }

            // 2. Text Search (title or description)
            val needle = q.trim().lowercase()
            val afterText = if (needle.isEmpty())
            {
                afterCat
            }
            else
            {
                afterCat.filter { it.title.lowercase().contains(needle) || it.description.lowercase().contains(needle) }
            }
            // 3. Favorites-only filter
            if (!favOnly)
            {
                afterText
            }
            else
            {
                afterText.filter {fav.contains(it.title)}
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ItemRepository.items
        )
    val favourites: StateFlow<Set<String>> = _favourites

    // --- Functions called by UI ---

    // Flip between Grid and List
    fun toggleLayout() {
        _isGrid.value = !_isGrid.value
    }

    // Update currently selected category (or reset with null).
    fun setCategory(cat: Category?) {
        _selectedCategory.value = cat
    }

    fun setQuery(q: String) {
        _query.value = q
    }

    fun setFavourites(titles: Set<String>)
    {
        _favourites.value = titles
    }
    fun setFavouritesOnly(on: Boolean) {
        _favouritesOnly.value = on
    }

    fun toggleFavouritesOnly() {
        _favouritesOnly.value = !_favouritesOnly.value
    }

    fun isFavourite(item: CatalogItem): Boolean = _favourites.value.contains(item.title)
    fun isFavouriteTitle(title: String): Boolean = _favourites.value.contains(title)

    fun toggleFavourite(item: CatalogItem){
        toggleFavouriteByTitle(item.title)
    }

    fun toggleFavouriteByTitle(title: String){
        val cur = _favourites.value
        _favourites.value = if(cur.contains(title))
        {
            cur - title
        }
        else
        {
            cur + title
        }
    }

    fun setLayout(isGrid: Boolean)
    {
        _isGrid.value = isGrid
    }
}