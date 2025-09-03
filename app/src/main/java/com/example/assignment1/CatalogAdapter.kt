/* Class File: CatalogAdapter.kt
*       - RecyclerView adapter that displays items in either grid or list format
*       - Handles item clicks
*
*  Date created: 29/08/2025
*  Last modified: 29/08/2025 */

package com.example.assignment1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CatalogAdapter(
    private val onItemClick: (CatalogItem) -> Unit,
    private val isFavourite: (CatalogItem) -> Boolean,
    private val onToggleFavourite: (CatalogItem) -> Unit,
    isGridInitial: Boolean = true
) : ListAdapter<CatalogItem, CatalogAdapter.VH>(DIFF) {

    var isGridMode: Boolean = isGridInitial
        set(value) {
            field = value
            notifyDataSetChanged()      // for switching layouts, dataset still diffed
        }

    override fun getItemViewType(position: Int): Int {
        return if (isGridMode) R.layout.item_catalog_grid else R.layout.item_catalog_list
    }

    override fun onCreateViewHolder(parent: ViewGroup, @LayoutRes viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return VH(view, onItemClick, isFavourite, onToggleFavourite)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(
        itemView: View,
        private val onClick: (CatalogItem) -> Unit,
        private val isFav: (CatalogItem) -> Boolean,
        private val onToggleFav: (CatalogItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
            private val iv: ImageView = itemView.findViewById(R.id.iv)
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
            private val chip: TextView = itemView.findViewById(R.id.chipCategory)
            private val btnFav: ImageButton? = itemView.findViewById(R.id.btnFav)

            fun bind(item: CatalogItem) {
                tvTitle.text = item.title
                tvDesc.text = item.description
                chip.text = item.category.displayName

                // Resolve drawable by name, falls back to placeholder
                val resId = itemView.resources.getIdentifier(
                    item.imageName,
                    "drawable",
                    itemView.context.packageName
                )
                iv.setImageResource(if (resId != 0) resId else R.drawable.placeholder)

                // Row click -> detail
                itemView.setOnClickListener { onClick(item) }

                // Heart icon -> reflects current fav state, toggles on press
                btnFav?.apply {
                    setImageResource(if (isFav(item)) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24)
                    setOnClickListener {
                        onToggleFav(item)
                        //update the icon immediately
                        setImageResource(if (isFav(item)) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24)
                    }
                }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CatalogItem>() {
            override fun areItemsTheSame(old: CatalogItem, new: CatalogItem) =
                old.title == new.title      // titles unique enough here
            override fun areContentsTheSame(old: CatalogItem, new: CatalogItem) = old == new
        }
    }
}