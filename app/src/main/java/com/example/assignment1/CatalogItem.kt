/* Data model class: (CatalogItem)
    - Defines the data model for one catalog entry
    - (title, description, category, image name).

*  Date created: 29/08/2025
*  Last modified: 29/08/2025 */

package com.example.assignment1

data class CatalogItem(
    val title: String,
    val description: String,
    val category: Category,
    /** Drawable resource name without the extension, e.g., "huong_viet_image1" */
    val imageName: String
)
