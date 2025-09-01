/* Object File: ItemRepository.kt
*       - Hard-coded list of CatalogItem objects
*
*  Date created: 29/08/2025
*  Last modified: 01/09/2025 */

package com.example.assignment1

object ItemRepository {

    // Compact set so app compiles; adding all items later
    val items: List<CatalogItem> = listOf(

        // Vietnamese
        CatalogItem(
            title = "Huong Viet Noodle House",
            description = "Famous for classic Vietnamese noodle dishes and comforting broths.",
            category = Category.VIETNAMESE,
            imageName = "huong_viet_logo"
        ),
        CatalogItem(
            title = "Viet Hoa",
            description = "Family-run restaurant serving authentic Vietnamese food with homemade noodles and house-roasted duck.",
            category = Category.VIETNAMESE,
            imageName = "viethoa_sign"
        ),
        CatalogItem(
            title = "Little Viet Restaurant",
            description = "Each dish a unique experience with a memorable culinary adventure that respects and celebrates the authenticity of Vietnamese flavours.",
            category = Category.VIETNAMESE,
            imageName = "lvr_logo"
        ),
        CatalogItem(
            title = "Pho Nguyen",
            description = "Pride of Nguyen family's son being the quintessential broth that has been perfected for more than 30 years.",
            category = Category.VIETNAMESE,
            imageName = "phonguyen_logo"
        ),
        CatalogItem(
            title = "House of Pho",
            description = "Authentic Vietnamese restaurant to savour South Vietnamese cuisine's rich tapestry, whispering tradition and tantalising with flavours.",
            category = Category.VIETNAMESE,
            imageName = "house_of_pho_logo"
        ),

        // Italian
        CatalogItem(
            title = "La Sosta",
            description = "Fremantle stopover for generous handmade pasta, irresistible desserts, and an extensive wine list.",
            category = Category.ITALIAN,
            imageName = "lasosta_logo"
        ),
        CatalogItem(
            title = "Bistro Bellavista",
            description = "A family owned Italian bistro serving authentic homemande Italian food and woodfired PIzza, offering friendly service and meal options.",
            category = Category.ITALIAN,
            imageName = "bistrob_logo"
        ),
        CatalogItem(
            title = "Garum",
            description = "A taste of Rome focused on the ideals of Roman cooking, its techniques and history.",
            category = Category.ITALIAN,
            imageName = "garum_image"
        ),
        CatalogItem(
            title = "Vin Populi",
            description = "A stylish Italian wine bar with a '60s feel, offering handmade pasta, antipastic, and a rotating wine list, with a focus on Italian recipes.",
            category = Category.ITALIAN,
            imageName = "vin_populi_logo"
        ),
        CatalogItem(
            title = "Spritz Spizzicheria",
            description = "Pasta, pizza and other traditional Italian cuisine in a relaxed place with wine and an industrial vibe.",
            category = Category.ITALIAN,
            imageName = "spritz_logo"
        ),

        // Japanese
        CatalogItem(
            title = "JBento",
            description = "Modern Japanese bento & izakaya dishes made with fresh local produce in Mount Pleasant.",
            category = Category.JAPANESE,
            imageName = "jbento_logo"
        ),
        CatalogItem(
            title = "KATEN",
            description = "KATEN offers a vibrant dining experience with a menu featuring a variety of katsu dishes with the ambiance enhanced by anime figures and posters.",
            category = Category.JAPANESE,
            imageName = "katen_logo"
        ),
        CatalogItem(
            title = "Hiyori Japanese Bar & Restaurant",
            description = "A modern Japanese dining experience witha  focus on simple and local, inviting customers to enjoy the purity and balance of authentic Japanese cuisine.",
            category = Category.JAPANESE,
            imageName = "hiyori_logo"
        ),
        CatalogItem(
            title = "Izakaya Tori Victoria Park",
            description = "A casual bar and diner to enjoy delicious fried chickem skewers, beers and cocktails. Izakaya Tori offers a vibrant dining experience with vibes reminiscent of Japan.",
            category = Category.JAPANESE,
            imageName = "izakayatori_logo"
        ),
        CatalogItem(
            title = "James Parker Sushi & Sake",
            description = "Inspired by Japanese traditional dining-style, James Parker offers the finest Japanese delicacies in a great and unique atmosphere.",
            category = Category.JAPANESE,
            imageName = "jamesparker_logo"
        ),

        // Indian
        CatalogItem(
            title = "Chakra Restaurant",
            description = "Progressive family-run Indian dishes with house-made elements and bold, beautifully presented flavours.",
            category = Category.INDIAN,
            imageName = "chakra_logo"
        ),
        CatalogItem(
            title = "Stonewater Indian Restaurant",
            description = "Experience the finest Indian cuisine and wines at Stonewater to discover the authentic flavours and a cozy ambiance.",
            category = Category.INDIAN,
            imageName = "stonewater_logo"
        ),
        CatalogItem(
            title = "The Grand Castle Indian Restaurant and Banquet",
            description = "Discover the authentic taste of handmade Indian sweets at Grand Castle Restaurant & Banquet, experience the authentic taste of India where the chefs use traditional cooking methods.",
            category = Category.INDIAN,
            imageName = "grandcastle_image"
        ),
        CatalogItem(
            title = "Ayini Flavours of India",
            description = "AYINI Flavours of India brings the authentic taste of India to your table with a wide range of delicious, traditional Indian cuisine.",
            category = Category.INDIAN,
            imageName = "ayini_logo"
        ),
        CatalogItem(
            title = "Spicy Affair Indian Cuisine",
            description = "Savour the rich, authentic flavours of India. From aromatic curries to sizzling tandoor, every bite is a journey to India.",
            category = Category.INDIAN,
            imageName = "spicyaffair_logo"
        ),

        // Chinese
        CatalogItem (
            title = "Chin's Noodle House",
            description = "A prominent Chinese Restaurant located in the heart of Leeming offering traditional Malaysian Chinese style cuisines including roast BBQ meats and banquet experiences.",
            category = Category.CHINESE,
            imageName = "chins_logo"
        ),
        CatalogItem(
            title = "Billy Lee's Chinese Restaurant",
            description = "At Billy Lee's, they bring the heart and soul of Chinese cuisine to Perth, offering a dining experience that blends tradition with modern flair.",
            category = Category.CHINESE,
            imageName = "billylees_logo"
        ),
        CatalogItem(
            title = "Pearl River Restaurant",
            description = "Pearl River Restaurant specialises on Dim-Sum and Cantonese Cuisine where you will be impressed with their culinary art and experience.",
            category = Category.CHINESE,
            imageName = "pearlriver_logo"
        ),
        CatalogItem(
            title = "Pearl on the Point",
            description = "Where authentic dim sum delight awaits at a premier destination for exquisite dim sum dining celebrating the rich flavours and traditions of Chinese cuisine.",
            category = Category.CHINESE,
            imageName = "pearlotp_logo"
        ),
        CatalogItem(
            title = "Grand Orient Restaurant",
            description = "The Grand Orient is a unique Chinese Restaurant experience and is inspired by the old-world, the menu is based on Cantonese flavours using modern and tradition cooking techniques.",
            category = Category.CHINESE,
            imageName = "grandorient_logo"
        ),

        // Thai
        CatalogItem(
            title = "Rym Tarng",
            description = "Rym Tarng is fresh, authentic, delightful, bringing Thai flavours to your plate and inspired by Thai street food.",
            category = Category.THAI,
            imageName = "rymtarng_logo"
        ),
        CatalogItem(
            title = "Paste Thai Restaurant",
            description = "Serves authentic delicious Thai food in a relaxed atmosphere designed to cater for all tastes enhanced with tradiitonal flvaours and tastes of delicate ingredients and the freshest herbs.",
            category = Category.THAI,
            imageName = "pastethai_logo"
        ),
        CatalogItem(
            title = "Louder Louder",
            description = "A contemporary Issan-Thai restaurant with recipes handed down from generation to generation to indulge you with dishes that 'taste just like Thailand'.",
            category = Category.THAI,
            imageName = "louderlouder_logo"
        ),
        CatalogItem(
            title = "Bangkok Brothers",
            description = "A place that is buzzing with the energy, fun and flavours of Thailand's favourite city! With tasty and inventive menus that change throughout the day.",
            category = Category.THAI,
            imageName = "bangkokbrothers_logo"
        ),
        CatalogItem(
            title = "Kub Kao Mt Lawley",
            description = "Where you enjoy a fascinating dining experience offering the inspiration from Thai traditions and western cultures that create the full flavour of tastiness.",
            category = Category.THAI,
            imageName = "kubkao_logo"
        )
    )
}