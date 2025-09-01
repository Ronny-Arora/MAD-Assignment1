/* Class File: CatalogViewModel.kt
*       - Manages app state.
*       - (Gird/list mode, selected category, filtered item list).
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

    // --- Source Data (Local in-memory list) ---

    // All catalog items (hard-coded in ItemRepository)
    private val _allItems = MutableStateFlow(ItemRepository.items)

    // --- Apply exposed filtered list ---

    // Combine items list with selected category.
    // Every time either changes, UI gets an updated filtered list.
    val items: StateFlow<List<CatalogItem>> =
        combine(_allItems, _selectedCategory) { list, cat ->
            if (cat == null) list   // no category filter -> return all
            else list.filter { it.category == cat } // filter items matching category
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ItemRepository.items
        )

    // --- Functions called by UI ---

    // Flip between Grid and List
    fun toggleLayout() {
        _isGrid.value = !_isGrid.value
    }

    // Update currently selected category (or reset with null).
    fun setCategory(cat: Category?) {
        _selectedCategory.value = cat
    }
}