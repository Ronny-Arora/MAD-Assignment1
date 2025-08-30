/* Class File: ListFragment.kt
*       - UI screen that shows catalog list, chips for categories,
*       - and the toggle between grid and list.
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch

// Fragment that shows the catalog list, category chips, and the grid/list toggle
class ListFragment : Fragment(R.layout.fragment_list) {

    // Attach a ViewModel scoped to this fragment
    private val vm: CatalogViewModel by viewModels()

    // References to UI widgets
    private lateinit var rv: RecyclerView
    private lateinit var switchLayout: MaterialSwitch

    // Category buttons
    private lateinit var btnAll: Button
    private lateinit var btnVietnamese: Button
    private lateinit var btnItalian: Button
    private lateinit var btnJapanese: Button
    private lateinit var btnChinese: Button
    private lateinit var btnThai: Button
    private lateinit var btnIndian: Button

    // RecyclerView adapter (shows catalog items)
    private val adapter = CatalogAdapter(
        onItemClick = { item ->
            // Show a Toast when clicked
            Toast.makeText(requireContext(), item.title, Toast.LENGTH_SHORT).show()
        },
        isGridInitial = true    // Default mode is Grid
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views from XML layout
        rv = view.findViewById(R.id.rv)
        switchLayout = view.findViewById(R.id.switchLayout)

        // find Buttons
        btnAll = view.findViewById(R.id.btnAll)
        btnVietnamese = view.findViewById(R.id.btnVietnamese)
        btnItalian = view.findViewById(R.id.btnItalian)
        btnJapanese = view.findViewById(R.id.btnJapanese)
        btnChinese = view.findViewById(R.id.btnChinese)
        btnThai = view.findViewById(R.id.btnThai)
        btnIndian = view.findViewById(R.id.btnIndian)

        // Hook up button clicks to update ViewModel filter
        btnAll.setOnClickListener { vm.setCategory(null) }
        btnVietnamese.setOnClickListener { vm.setCategory(Category.VIETNAMESE) }
        btnItalian.setOnClickListener { vm.setCategory(Category.ITALIAN) }
        btnJapanese.setOnClickListener {vm.setCategory(Category.JAPANESE) }
        btnChinese.setOnClickListener { vm.setCategory(Category.CHINESE) }
        btnThai.setOnClickListener { vm.setCategory(Category.THAI) }
        btnIndian.setOnClickListener { vm.setCategory(Category.INDIAN) }

        // Set up RecyclerView
        rv.adapter = adapter
        bindLayoutManager(isGrid = true)    // Start in Grid layout

        // Switch: Grid/List
        switchLayout.setOnCheckedChangedListener { _: CompoundButton, _: Boolean ->
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
            }
        }
    }

    // Helper to switch between GridLayoutManager and LinearLayoutManager
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