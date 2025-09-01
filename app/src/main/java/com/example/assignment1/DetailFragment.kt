/* Class File: DetailFragment.kt
    - Fragment that shows the full detail view of a restaurant
    - (Large image, title, category badge as button, description).
*
*  Date created: 01/09/2025
*  Last modified: 01/09/2025 */

package com.example.assignment1

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment

class DetailFragment : Fragment(R.layout.fragment_detail) {

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESC = "arg_desc"
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_IMAGE = "arg_image"

        fun newInstance(item: CatalogItem) = DetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE,item.title)
                putString(ARG_DESC, item.description)
                putString(ARG_CATEGORY, item.category.displayName)
                putString(ARG_IMAGE, item.imageName)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDesc = view.findViewById<TextView>(R.id.tvDesc)
        val btnCategory = view.findViewById<MaterialButton>(R.id.btnCategoryBadge)
        val ivHero = view.findViewById<ImageView>(R.id.ivHero)

        val title = requireArguments().getString(ARG_TITLE)!!
        val desc = requireArguments().getString(ARG_DESC)!!
        val cat = requireArguments().getString(ARG_CATEGORY)!!
        val imageName = requireArguments().getString(ARG_IMAGE)!!

        tvTitle.text = title
        tvDesc.text = desc
        btnCategory.text = cat      // read-only badge

        val resId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
        ivHero.setImageResource(if (resId != 0) resId else R.drawable.placeholder)
    }
}