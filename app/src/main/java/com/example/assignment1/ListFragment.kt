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
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
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
import androidx.fragment.app.activityViewModels
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.content.Context

// Fragment that shows the catalog list, category chips, and the grid/list toggle
class ListFragment : Fragment(R.layout.fragment_list) {

    // ACTIVITY-Scoped VM so favourites & filters are shared with DetailFragments
    private val vm: CatalogViewModel by activityViewModels()

    // References to UI widgets
    private lateinit var rv: RecyclerView
    private lateinit var switchLayout: MaterialSwitch

    // Category buttons
    private lateinit var btnFavourites: MaterialButton
    private lateinit var btnAll: MaterialButton
    private lateinit var btnVietnamese: MaterialButton
    private lateinit var btnItalian: MaterialButton
    private lateinit var btnJapanese: MaterialButton
    private lateinit var btnChinese: MaterialButton
    private lateinit var btnThai: MaterialButton
    private lateinit var btnIndian: MaterialButton

    // Navigation to detail
    private lateinit var adapter: CatalogAdapter



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views from XML layout
        rv = view.findViewById(R.id.rv)
        switchLayout = view.findViewById(R.id.switchLayout)

        val header = view.findViewById<View>(R.id.header)
        val headerInitialTop = header.paddingTop
        val rvInitialBottom = rv.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view){_, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header.updatePadding(top = headerInitialTop + sysBars.top)
            rv.updatePadding(bottom = rvInitialBottom + sysBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(view)

        // Find search box
        val etSearch = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearch)
        etSearch.addTextChangedListener { text ->
            vm.setQuery(text?.toString().orEmpty())
        }

        // find category Buttons
        btnFavourites = view.findViewById(R.id.btnFavourites)
        btnAll = view.findViewById(R.id.btnAll)
        btnVietnamese = view.findViewById(R.id.btnVietnamese)
        btnItalian = view.findViewById(R.id.btnItalian)
        btnJapanese = view.findViewById(R.id.btnJapanese)
        btnChinese = view.findViewById(R.id.btnChinese)
        btnThai = view.findViewById(R.id.btnThai)
        btnIndian = view.findViewById(R.id.btnIndian)

        // Make buttons toggleable to show a "selected" state
        makeCheckable(btnFavourites, btnAll, btnVietnamese, btnItalian, btnJapanese, btnChinese, btnThai, btnIndian)

        // Button click listeners
        btnFavourites.setOnClickListener {
            vm.toggleFavouritesOnly()
            // When Favourites is ON, de-select category filter
        }
        btnAll.setOnClickListener {
            vm.setFavouritesOnly(false)
            vm.setCategory(null)
        }

        btnVietnamese.setOnClickListener { vm.setFavouritesOnly(false); vm.setCategory(Category.VIETNAMESE) }
        btnItalian.setOnClickListener { vm.setFavouritesOnly(false); vm.setCategory(Category.ITALIAN) }
        btnJapanese.setOnClickListener {vm.setFavouritesOnly(false); vm.setCategory(Category.JAPANESE) }
        btnChinese.setOnClickListener { vm.setFavouritesOnly(false); vm.setCategory(Category.CHINESE) }
        btnThai.setOnClickListener { vm.setFavouritesOnly(false); vm.setCategory(Category.THAI) }
        btnIndian.setOnClickListener { vm.setFavouritesOnly(false); vm.setCategory(Category.INDIAN) }

        // Adapter
        adapter = CatalogAdapter(
            onItemClick = { item ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, DetailFragment.newInstance(item))
                    .addToBackStack("detail")
                    .commit()
            },
            isFavourite = vm::isFavourite,
            onToggleFavourite = vm::toggleFavourite,
            isGridInitial = true
        )
        // Set up RecyclerView & switchLayout setup
        rv.adapter = adapter
        bindLayoutManager(isGrid = true)    // Start in Grid layout

        // Switch: Grid/List
        switchLayout.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            TransitionManager.beginDelayedTransition(rv, AutoTransition())
            vm.setLayout(isChecked)
        }

        // Collect state from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle = only collect when fragment visible/started
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Collect grid/list state
                launch {
                    vm.isGrid.collect { isGrid ->
                        // Update switch state
                        if (switchLayout.isChecked != isGrid)
                        {
                            switchLayout.isChecked = isGrid
                        }
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
                        updateButtonChecks(cat, vm.favouritesOnly.value)
                    }
                }

                launch {
                    vm.favouritesOnly.collect { favOnly ->
                        updateButtonChecks(vm.selectedCategory.value, favOnly)
                    }
                }
            }
        }
        val prefs = requireContext().getSharedPreferences("catalog_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("favourites", emptySet()) ?: emptySet()
        vm.setFavourites(saved)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    vm.favourites.collect { set ->
                        prefs.edit().putStringSet("favourites", set).apply()
                    }
                }
            }
        }

    }


    // Keep only the chosen button "checked" (green); others unchecked (blue)
    private fun updateButtonChecks(cat: Category?, favOnly: Boolean) {
        setChecked(btnFavourites, favOnly)
        // Reset all to unchecked first
        setChecked(btnAll, !favOnly && cat == null)
        setChecked(btnVietnamese, !favOnly && cat == Category.VIETNAMESE)
        setChecked(btnItalian, !favOnly && cat == Category.ITALIAN)
        setChecked(btnJapanese, !favOnly && cat == Category.JAPANESE)
        setChecked(btnChinese, !favOnly && cat == Category.CHINESE)
        setChecked(btnThai, !favOnly && cat == Category.THAI)
        setChecked(btnIndian, !favOnly && cat == Category.INDIAN)

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
        if(!isGrid)
        {
            rv.layoutManager = LinearLayoutManager(requireContext())
            return
        }

        // Initial span based on current width
        val initialSpan = computeSpanByWidthPx(rv.width)
        val glm = GridLayoutManager(requireContext(), if (initialSpan > 0) initialSpan else calculateSpanFallback())
        rv.layoutManager = glm

        // Recompute span when the RecyclerView's size changes (rotation, split screen, etc)
        rv.addOnLayoutChangeListener(object: View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                if (!vm.isGrid.value) return
                val newSpan = computeSpanByWidthPx(rv.width)
                val manager = rv.layoutManager as? GridLayoutManager?: return
                if(newSpan > 0 && manager.spanCount != newSpan)
                {
                    manager.spanCount = newSpan
                }
            }
        })
    }

    // Compute columns by actual px width so it adapts in split-screen
    private fun computeSpanByWidthPx(rvWidthPx: Int): Int {
        if (rvWidthPx <= 0)
        {
            return 0
        }
        val dm = resources.displayMetrics
        val density = dm.density
        val minCellDp = 168f // target minimum card width
        val minCellPx = (minCellDp * density)
        val available = (rvWidthPx - rv.paddingLeft - rv.paddingRight).coerceAtLeast(0)
        return (available /  minCellPx).toInt().coerceAtLeast(1)
    }

    private fun calculateSpanFallback(): Int {
        val widthDp = resources.configuration.screenWidthDp.takeIf { it > 0 } ?: 360
        val minCellDp = 168f
        return (widthDp / minCellDp).toInt().coerceAtLeast(1)
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