/* Object File: ItemRepository.kt
*       - Hard-coded list of CatalogItem objects
*
*  Date created: 29/08/2025
*  Last modified: 29/08/2025 */

package com.example.assignment1

object ItemRepository {

    // Compact set so app compiles; adding all items later
    val items: List<CatalogItem> = listOf(

        // Vietnamese
        CatalogItem(
            title = "Huong Viet Noodle House",
            description = "Famous for classic Vietnamese noodle dishes and comforting broths.",
            category = Category.VIETNAMESE,
            imageName = "huong_viet_image1"
        ),
        CatalogItem(
            title = "Viet Hoa",
            description = "Family-run restaurant serving authentic Vietnamese food with homemade noodles and house-roasted duck.",
            category = Category.VIETNAMESE,
            imageName = "viethoa_restaurantimage1"
        ),

        // Italian
        CatalogItem(
            title = "La Sosta",
            description = "Fremantle stopover for generous handmade pasta, irresistible desserts, and an extensive wine list.",
            category = Category.ITALIAN,
            imageName = "la_sosta_logo1"
        ),
        CatalogItem(
            title = "Garum",
            description = "A taste of Rome focused on the ideals of Roman cooking, its techniques and history.",
            category = Category.ITALIAN,
            imageName = "garum_image1"
        ),

        // Japanese
        CatalogItem(
            title = "JBento",
            description = "Modern Japanese bento & izakaya dishes made with fresh local produce in Mount Pleasant.",
            category = Category.JAPANESE,
            imageName = "jbento_logo1"
        ),

        // Indian
        CatalogItem(
            title = "Chakra Restaurant",
            description = "Progressive family-run Indian dishes with house-made elements and bold, beautifully presented flavours.",
            category = Category.INDIAN,
            imageName = "chakra_logo1"
        )
    )
}