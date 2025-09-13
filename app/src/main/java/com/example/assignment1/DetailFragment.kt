/* Class File: DetailFragment.kt
 *  - Full detail view: hero image, title, category badge, description
 *  - Favourite toggle (shared VM), distance from user, Open in Maps
 *
 *  Date created: 01/09/2025
 *  Last modified: 13/09/2025
 */

package com.example.assignment1

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DetailFragment : Fragment(R.layout.fragment_detail) {

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESC = "arg_desc"
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_IMAGE = "arg_image"

        fun newInstance(item: CatalogItem) = DetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, item.title)
                putString(ARG_DESC, item.description)
                putString(ARG_CATEGORY, item.category.displayName)
                putString(ARG_IMAGE, item.imageName)
            }
        }
    }

    private val vm: CatalogViewModel by activityViewModels()
    // If you added the runtime Places path:
    private val placesResolver by lazy { PlacesResolver(requireContext()) }

    // Keep a reference to the matched item (by title) so we can compute distance
    private var itemModel: CatalogItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDesc = view.findViewById<TextView>(R.id.tvDesc)
        val btnCategory = view.findViewById<MaterialButton>(R.id.btnCategoryBadge)
        val ivHero = view.findViewById<ImageView>(R.id.ivHero)
        val btnClose = view.findViewById<MaterialButton>(R.id.btnClose)
        val btnFavToggle = view.findViewById<MaterialButton>(R.id.btnFavToggle)

        // Optional (add these IDs in fragment_detail.xml):
        val tvDistance = view.findViewById<TextView?>(R.id.tvDistance)
        val btnOpenMaps = view.findViewById<MaterialButton?>(R.id.btnOpenMaps)

        // Safe area for cutouts & nav bars
        val content = view.findViewById<View>(R.id.detailContent)
        val initTop = content.paddingTop
        val initBottom = content.paddingBottom
        val initLeft = content.paddingLeft
        val initRight = content.paddingRight
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            content.updatePadding(
                top = initTop + bars.top,
                bottom = initBottom + bars.bottom,
                left = initLeft + bars.left,
                right = initRight + bars.right
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)

        // Args
        val title = requireArguments().getString(ARG_TITLE)!!
        val desc = requireArguments().getString(ARG_DESC)!!
        val cat = requireArguments().getString(ARG_CATEGORY)!!
        val imageName = requireArguments().getString(ARG_IMAGE)!!

        // Bind
        tvTitle.text = title
        tvDesc.text = desc
        btnCategory.text = cat

        val resId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
        ivHero.setImageResource(if (resId != 0) resId else R.drawable.placeholder)

        // Match the item model by title (so we can get lat/lng if present)
        itemModel = ItemRepository.items.find { it.title == title }

        // Close
        btnClose.setOnClickListener { parentFragmentManager.popBackStack() }

        // Favourites
        fun refreshFavButton() {
            val isFav = vm.isFavouriteTitle(title)
            btnFavToggle.text = if (isFav)
                getString(R.string.action_remove_favourite)
            else
                getString(R.string.action_add_favourite)
        }
        refreshFavButton()

        btnFavToggle.setOnClickListener {
            vm.toggleFavouriteByTitle(title)
            refreshFavButton()
        }

        // Distance UI
        fun formatDistance(km: Double): String =
            if (km < 1.0) "${(km * 1000).roundToInt()} m" else String.format("%.1f km", km)

        fun updateDistanceUI() {
            val km = itemModel?.let { vm.distanceKmFor(it) }
            if (tvDistance != null) {
                tvDistance.text = if (km != null) "Distance: ${formatDistance(km)}"
                else getString(R.string.distance_unknown)
            }
        }

        updateDistanceUI()
        // Recompute distance when user location changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.userLocation.collect { updateDistanceUI() }
                }
            }
        }

        // Open in Maps
        btnOpenMaps?.setOnClickListener {
            val item = itemModel
            val lat = item?.lat
            val lng = item?.lng
            if (lat != null && lng != null) {
                openInMaps(lat, lng, title)
            } else {
                // Resolve at runtime (non-blocking)
                viewLifecycleOwner.lifecycleScope.launch {
                    val pair = runCatching { placesResolver.resolveLatLng(title) }.getOrNull()
                    if (pair != null) {
                        openInMaps(pair.first, pair.second, title)
                    }
                }
            }
        }
    }

    private fun openInMaps(lat: Double, lng: Double, label: String) {
        val uri = Uri.parse("geo:$lat,$lng?q=${lat},${lng}(${Uri.encode(label)})")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
