/* Class File: ListFragment.kt
*       - UI screen that shows catalog list, MaterialButtons for categories,
*       - and the toggle between grid and list.
*       - Opens DetailFragment when a list item is chosen.
*
*  Date created: 30/08/2025
*  Last modified: 30/08/2025 */

package com.example.assignment1

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener

// Fragment that shows the catalog list, category chips, and the grid/list toggle
class ListFragment : Fragment(R.layout.fragment_list) {

    // Attach a ViewModel scoped to this fragment
    private val vm: CatalogViewModel by viewModels()

    // References to UI widgets
    private lateinit var rv: RecyclerView
    private lateinit var switchLayout: MaterialSwitch

    // Category buttons
    private lateinit var btnAll: MaterialButton
    private lateinit var btnVietnamese: MaterialButton
    private lateinit var btnItalian: MaterialButton
    private lateinit var btnJapanese: MaterialButton
    private lateinit var btnChinese: MaterialButton
    private lateinit var btnThai: MaterialButton
    private lateinit var btnIndian: MaterialButton

    // Navigation to detail
    private val adapter = CatalogAdapter(
        onItemClick = { item ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, DetailFragment.newInstance(item))
                .addToBackStack("detail")
                .commit()
        },
        isGridInitial = true    // Default mode is Grid
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views from XML layout
        rv = view.findViewById(R.id.rv)
        switchLayout = view.findViewById(R.id.switchLayout)

        // Find search box
        val etSearch = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearch)
        etSearch.addTextChangedListener { text ->
            vm.setQuery(text?.toString().orEmpty())
        }

        // find category Buttons
        btnAll = view.findViewById(R.id.btnAll)
        btnVietnamese = view.findViewById(R.id.btnVietnamese)
        btnItalian = view.findViewById(R.id.btnItalian)
        btnJapanese = view.findViewById(R.id.btnJapanese)
        btnChinese = view.findViewById(R.id.btnChinese)
        btnThai = view.findViewById(R.id.btnThai)
        btnIndian = view.findViewById(R.id.btnIndian)

        // Make buttons toggleable to show a "selected" state
        makeCheckable(btnAll, btnVietnamese, btnIndian, btnJapanese, btnChinese, btnThai, btnIndian)

        // Button click listeners
        btnAll.setOnClickListener { vm.setCategory(null) }
        btnVietnamese.setOnClickListener { vm.setCategory(Category.VIETNAMESE) }
        btnItalian.setOnClickListener { vm.setCategory(Category.ITALIAN) }
        btnJapanese.setOnClickListener {vm.setCategory(Category.JAPANESE) }
        btnChinese.setOnClickListener { vm.setCategory(Category.CHINESE) }
        btnThai.setOnClickListener { vm.setCategory(Category.THAI) }
        btnIndian.setOnClickListener { vm.setCategory(Category.INDIAN) }

        // Set up RecyclerView & switchLayout setup
        rv.adapter = adapter
        bindLayoutManager(isGrid = true)    // Start in Grid layout

        // Switch: Grid/List
        switchLayout.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            vm.toggleLayout()   // State drives the actual binding below
        }

        // Collect state from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle = only collect when fragment visible/started
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Collect grid/list state
                launch {
                    vm.isGrid.collect { isGrid ->
                        // Update switch state
                        switchLayout.isChecked = isGrid     // keep UI in sync
                        // Tell adapter which layout to use
                        adapter.isGridMode = isGrid
                        // Update the RecyclerView LayoutManager
                        bindLayoutManager(isGrid)
                    }
                }

                // Collect filtered items
                launch {
                    vm.items.collect { list ->
                        // Submit updated list to adapter (DiffUtil handles differences)
                        adapter.submitList(list)
                    }
                }

                // Selected category -> reflect on button checked states
                launch {
                    vm.selectedCategory.collect { cat ->
                        updateButtonChecks(cat)
                    }
                }
            }
        }
    }

    // Toggle a category; ensures single selection and updates ViewModel
    private fun selectCategory(cat: Category?) {
        vm.setCategory(cat)
        updateButtonChecks(cat)     // immediate visual feedback
    }

    // Keep only the chosen button "checked" (green); others unchecked (blue)
    private fun updateButtonChecks(cat: Category?) {
        // Reset all to unchecked first
        setChecked(btnAll, false)
        setChecked(btnVietnamese, false)
        setChecked(btnItalian, false)
        setChecked(btnJapanese, false)
        setChecked(btnChinese, false)
        setChecked(btnThai, false)
        setChecked(btnIndian, false)

        // Turn on just the active one
        when (cat) {
            null -> setChecked(btnAll, true)
            Category.VIETNAMESE -> setChecked(btnVietnamese, true)
            Category.ITALIAN -> setChecked(btnItalian, true)
            Category.JAPANESE -> setChecked(btnJapanese, true)
            Category.CHINESE -> setChecked(btnChinese, true)
            Category.THAI -> setChecked(btnThai, true)
            Category.INDIAN -> setChecked(btnIndian, true)
        }
    }

    private fun setChecked(btn: MaterialButton, checked: Boolean) {
        // isCheckable must be true for state lists to show selected colours
        if (!btn.isCheckable) btn.isCheckable = true
        btn.isChecked = checked
    }

    private fun makeCheckable(vararg buttons: MaterialButton) {
        buttons.forEach { it.isCheckable = true }
    }

    // Switch between GridLayoutManager and LinearLayoutManager
    private fun bindLayoutManager(isGrid: Boolean) {
        if (!isGrid) {
            // LinearLayoutManager -> vertical list
            rv.layoutManager = LinearLayoutManager(requireContext())
            return
        }
        // GridLayoutManager with span count depending on screen/orientation
        val span = calculateSpanCount()
        rv.layoutManager = GridLayoutManager(requireContext(), span)
    }

    // Calculate how many columns Grid should have
    private fun calculateSpanCount(): Int {
        val cfg = resources.configuration
        val isLandscape = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE
        val sw = cfg.smallestScreenWidthDp

        return when {
            sw >= 600 -> 4      // tablets -> 3-4; choose 4 as default
            isLandscape -> 3    // phone landscape
            else -> 2           // phone portrait
        }
    }
}