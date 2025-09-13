/* Class File: ListFragment.kt
 *  - UI screen that shows catalog list, category buttons,
 *  - a toggle between grid and list,
 *  - supports favourites with persistence and "near me".
 *
 *  Date created: 30/08/2025
 *  Last modified: 14/09/2025
 */

package com.example.assignment1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.appcompat.widget.PopupMenu

class ListFragment : Fragment(R.layout.fragment_list) {

    // Single listener instance to avoid stacking multiple layout listeners
    private var widthListener: View.OnLayoutChangeListener? = null

    // Activity-scoped VM so DetailFragment and this fragment share state
    private val vm: CatalogViewModel by activityViewModels()

    // Views
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
    private lateinit var btnNearMe: MaterialButton

    // Adapter
    private lateinit var adapter: CatalogAdapter
    private lateinit var btnDistance: MaterialButton

    // Places resolver (reads your API key from strings.xml)
    private val placesResolver by lazy {
        PlacesResolver(requireContext(), getString(R.string.google_maps_key))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        rv = view.findViewById(R.id.rv)
        switchLayout = view.findViewById(R.id.switchLayout)

        // Safe-area handling (status/cutout top for header, nav bar bottom for list)
        val header = view.findViewById<View>(R.id.header)
        val headerInitialTop = header.paddingTop
        val rvInitialBottom = rv.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header.updatePadding(top = headerInitialTop + sysBars.top)
            rv.updatePadding(bottom = rvInitialBottom + sysBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(view)

        // Search
        val etSearch = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearch)
        etSearch.addTextChangedListener { text -> vm.setQuery(text?.toString().orEmpty()) }

        // Buttons
        btnFavourites = view.findViewById(R.id.btnFavourites)
        btnAll = view.findViewById(R.id.btnAll)
        btnVietnamese = view.findViewById(R.id.btnVietnamese)
        btnItalian = view.findViewById(R.id.btnItalian)
        btnJapanese = view.findViewById(R.id.btnJapanese)
        btnChinese = view.findViewById(R.id.btnChinese)
        btnThai = view.findViewById(R.id.btnThai)
        btnIndian = view.findViewById(R.id.btnIndian)
        btnNearMe = view.findViewById(R.id.btnNearMe)
        btnDistance = view.findViewById(R.id.btnDistance)
        makeCheckable(btnNearMe, btnFavourites, btnAll, btnVietnamese, btnItalian, btnJapanese, btnChinese, btnThai, btnIndian, btnDistance)

        // Near me: request location then enable sort/filter
        btnNearMe.setOnClickListener {
            if (hasLocationPermission()) {
                fetchLocationAndEnableNearMe()
            } else {
                requestLocationPerms.launch(locationPerms)
            }
        }

        // Favourites: when turning ON, clear other filters to avoid empty results
        btnFavourites.setOnClickListener {
            val turningOn = !vm.favouritesOnly.value
            vm.setFavouritesOnly(turningOn)
            if (turningOn) {
                vm.setCategory(null)
                vm.setQuery("")
                etSearch.setText("")
            }
        }
        btnAll.setOnClickListener {
            vm.setFavouritesOnly(false)
            vm.setCategory(null)
        }
        btnVietnamese.setOnClickListener { vm.setFavouritesOnly(false); vm.setCategory(Category.VIETNAMESE) }
        btnItalian.setOnClickListener   { vm.setFavouritesOnly(false); vm.setCategory(Category.ITALIAN) }
        btnJapanese.setOnClickListener  { vm.setFavouritesOnly(false); vm.setCategory(Category.JAPANESE) }
        btnChinese.setOnClickListener   { vm.setFavouritesOnly(false); vm.setCategory(Category.CHINESE) }
        btnThai.setOnClickListener      { vm.setFavouritesOnly(false); vm.setCategory(Category.THAI) }
        btnIndian.setOnClickListener    { vm.setFavouritesOnly(false); vm.setCategory(Category.INDIAN) }

        btnDistance.setOnClickListener { anchor ->
            val menu = PopupMenu(requireContext(), anchor)
            val ID_NEAREST = 1
            val ID_FURTHEST = 2
            val ID_OFF = 3
            val ID_NEARBY_TOGGLE = 4
            val ID_REFRESH_LOC = 5
            val ID_CLEAR_LOC = 6

            menu.menu.add(0, ID_NEAREST, 0, getString(R.string.distance_sort_nearest))
            menu.menu.add(0, ID_FURTHEST, 1, getString(R.string.distance_sort_furthest))
            menu.menu.add(0, ID_OFF, 2, getString(R.string.distance_sort_off))
            menu.menu.add(0, ID_NEARBY_TOGGLE, 3, getString(R.string.distance_nearby_toggle))
            menu.menu.add(0, ID_REFRESH_LOC, 4, getString(R.string.distance_refresh_location))
            menu.menu.add(0, ID_CLEAR_LOC, 5, getString(R.string.distance_clear_location))

            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    ID_NEAREST -> vm.setDistanceSort(CatalogViewModel.DistanceSort.NEAREST)
                    ID_FURTHEST -> vm.setDistanceSort(CatalogViewModel.DistanceSort.FURTHEST)
                    ID_OFF -> vm.setDistanceSort(CatalogViewModel.DistanceSort.NONE)
                    ID_NEARBY_TOGGLE -> vm.setNearbyOnly(!vm.nearbyOnly.value)
                    ID_REFRESH_LOC -> fetchLocationAndEnableNearMe()
                    ID_CLEAR_LOC -> {
                        vm.setDistanceSort(CatalogViewModel.DistanceSort.NONE)
                        vm.setNearbyOnly(false)
                        vm.setUserLocation(lat = Double.NaN, lng = Double.NaN)
                    }
                }
                true
            }
            menu.show()
        }

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
            distanceKm = vm::distanceKmFor,
            isGridInitial = true
        )
        rv.adapter = adapter
        bindLayoutManager(isGrid = true) // default grid

        // Grid/List switch: set explicit state (no toggle loop)
        switchLayout.setOnCheckedChangeListener { _, isChecked ->
            TransitionManager.beginDelayedTransition(rv, AutoTransition())
            vm.setLayout(isChecked)
        }

        // Optional persistence of favourites across app restarts
        val prefs = requireContext().getSharedPreferences("catalog_prefs", Context.MODE_PRIVATE)
        prefs.getStringSet("favourites", null)?.toSet()?.let { saved ->
            if (vm.favourites.value.isEmpty()) vm.setFavourites(saved)
        }

        // Collect state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Layout mode
                launch {
                    vm.isGrid.collect { isGrid ->
                        if (switchLayout.isChecked != isGrid) {
                            switchLayout.isChecked = isGrid
                        }
                        adapter.isGridMode = isGrid
                        bindLayoutManager(isGrid)
                    }
                }

                // Items (filters applied)
                launch {
                    vm.items.collect { list ->
                        adapter.submitList(list)
                    }
                }

                // Button checked states
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

                // Favourites set changed -> save + redraw hearts
                launch {
                    vm.favourites.collect { set ->
                        prefs.edit { putStringSet("favourites", set) }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    // --- helpers ---

    private fun updateButtonChecks(cat: Category?, favOnly: Boolean) {
        setChecked(btnFavourites, favOnly)
        setChecked(btnAll, !favOnly && cat == null)
        setChecked(btnVietnamese, !favOnly && cat == Category.VIETNAMESE)
        setChecked(btnItalian,   !favOnly && cat == Category.ITALIAN)
        setChecked(btnJapanese,  !favOnly && cat == Category.JAPANESE)
        setChecked(btnChinese,   !favOnly && cat == Category.CHINESE)
        setChecked(btnThai,      !favOnly && cat == Category.THAI)
        setChecked(btnIndian,    !favOnly && cat == Category.INDIAN)
    }

    private fun setChecked(btn: MaterialButton, checked: Boolean) {
        if (!btn.isCheckable) btn.isCheckable = true
        btn.isChecked = checked
    }

    private fun makeCheckable(vararg buttons: MaterialButton) {
        buttons.forEach { it.isCheckable = true }
    }

    // Bind layout manager and keep one width listener for adaptive spans
    private fun bindLayoutManager(isGrid: Boolean) {
        if (!isGrid) {
            rv.layoutManager = LinearLayoutManager(requireContext())
            widthListener?.let { rv.removeOnLayoutChangeListener(it) }
            widthListener = null
            return
        }

        val initialSpan = computeSpanByWidthPx(rv.width)
        val glm = GridLayoutManager(
            requireContext(),
            if (initialSpan > 0) initialSpan else calculateSpanFallback()
        )
        rv.layoutManager = glm

        // Replace any existing listener with a fresh one
        widthListener?.let { rv.removeOnLayoutChangeListener(it) }
        widthListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (!vm.isGrid.value) return@OnLayoutChangeListener
            val newSpan = computeSpanByWidthPx(rv.width)
            (rv.layoutManager as? GridLayoutManager)?.let { manager ->
                if (newSpan > 0 && manager.spanCount != newSpan) {
                    manager.spanCount = newSpan
                }
            }
        }
        rv.addOnLayoutChangeListener(widthListener)
    }

    /** Compute columns by actual px width so it adapts in split-screen. */
    private fun computeSpanByWidthPx(rvWidthPx: Int): Int {
        if (rvWidthPx <= 0) return 0
        val density = resources.displayMetrics.density
        val minCellDp = 168f // target min card width (tweak 160–200 as needed)
        val minCellPx = (minCellDp * density)
        val available = (rvWidthPx - rv.paddingLeft - rv.paddingRight).coerceAtLeast(0)
        return (available / minCellPx).toInt().coerceAtLeast(1)
    }

    /** Fallback when width isn’t known yet — uses screenWidthDp. */
    private fun calculateSpanFallback(): Int {
        val widthDp = resources.configuration.screenWidthDp.takeIf { it > 0 } ?: 360
        val minCellDp = 168f
        return (widthDp / minCellDp).toInt().coerceAtLeast(1)
    }

    override fun onDestroyView() {
        // Clean up listener to avoid leaks
        widthListener?.let { rv.removeOnLayoutChangeListener(it) }
        widthListener = null
        super.onDestroyView()
    }

    // --- Location / Places ---

    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    private val locationPerms = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestLocationPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants.any { it.value }
        if (granted) {
            fetchLocationAndEnableNearMe()
        }
        // else: user denied; no-op (you could show a Snackbar)
    }

    private fun hasLocationPermission(): Boolean =
        locationPerms.any { perm ->
            ContextCompat.checkSelfPermission(requireContext(), perm) == PackageManager.PERMISSION_GRANTED
        }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndEnableNearMe() {
        fetchLocation { lat, lng ->
            vm.setUserLocation(lat, lng)
            vm.setDistanceSort(CatalogViewModel.DistanceSort.NEAREST) // NEW API
            btnNearMe.isChecked = true
            vm.setFavouritesOnly(false)
            vm.setCategory(null)

            // Resolve missing coords progressively -> live re-sort as they arrive
            viewLifecycleOwner.lifecycleScope.launch {
                val toResolve = vm.items.value.filter { it.lat == null || it.lng == null }
                for (item in toResolve) {
                    runCatching { placesResolver.resolveLatLng(item.title) }
                        .getOrNull()
                        ?.let { (rLat, rLng) -> vm.upsertResolvedCoord(item.title, rLat, rLng) }
                    delay(150L)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation(onReady: (Double, Double) -> Unit) {
        locationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    onReady(loc.latitude, loc.longitude)
                } else {
                    locationClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null) onReady(last.latitude, last.longitude)
                    }
                }
            }
    }
}
