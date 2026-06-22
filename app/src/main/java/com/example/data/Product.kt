package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val displayName: String, val icon: String) {
    HAIR("Grooming", "spa"),
    FASHION("Fashion", "dry_cleaning")
}

data class Product(
    val id: String,
    val name: String,
    val category: Category,
    val subcategory: String,
    val price: Long,
    val rating: Float,
    val description: String,
    val sizes: List<String> = emptyList(),
    val tag: String? = null,
    val details: Map<String, String> = emptyMap()
)

object ProductRepository {
    val sampleProducts = listOf(
        // === HAIR GROOMING ===
        Product(
            id = "pomade-01",
            name = "KULTUR Water-Based Pomade",
            category = Category.HAIR,
            subcategory = "Pomade Water Based",
            price = 145000,
            rating = 4.9f,
            description = "Pomade water-based dengan daya rekat tinggi (heavy hold) dan kemilau natural (slick shine). Mudah dibilas dengan sekali keramas, menjaga kulit kepala tetap segar dengan aroma bay rum segar.",
            sizes = listOf("100g", "150g"),
            tag = "BEST SELLER",
            details = mapOf(
                "Hold" to "Heavy Hold (5/5)",
                "Shine" to "Medium-Slick (3/5)",
                "Aroma" to "Spiced Bay Rum & Bergamot",
                "Ingredients" to "Water-soluble, Panthenol, Keratins"
            )
        ),
        Product(
            id = "clay-02",
            name = "Matte Grit Texture Clay",
            category = Category.HAIR,
            subcategory = "Clay",
            price = 155000,
            rating = 4.8f,
            description = "Finishing matte natural dengan tekstur bervolume maksimal. Diperkaya dengan kaolin clay alami untuk menyerap minyak berlebih, memberikan tatanan rambut yang bisa ditata ulang sepanjang hari.",
            sizes = listOf("85g"),
            tag = "TRENDING",
            details = mapOf(
                "Hold" to "Extreme Hold (5/5)",
                "Shine" to "Full Matte (0/5)",
                "Aroma" to "Smoked Wood & Eucalyptus",
                "Ingredients" to "Bentomite & Kaolin Clay, Shea Butter"
            )
        ),
        Product(
            id = "powder-03",
            name = "KULTUR Volumizing Hair Powder",
            category = Category.HAIR,
            subcategory = "Powder",
            price = 120000,
            rating = 4.7f,
            description = "Bedak penata rambut ultra-ringan memberikan volume instan seketika meluncur di akar rambut. Sempurna untuk styling model messy, crop cut, atau textured quiff kekinian.",
            sizes = listOf("20g"),
            tag = "NEW ARIVAL",
            details = mapOf(
                "Hold" to "Light-Flexible (3/5)",
                "Shine" to "Matte Finish (1/5)",
                "Aroma" to "Fresh Lemon Lavender",
                "Ingredients" to "Silica Silylate, Citric Acid"
            )
        ),
        Product(
            id = "spray-04",
            name = "Locked-In Freeze Hair Spray",
            category = Category.HAIR,
            subcategory = "Hair Spray",
            price = 160000,
            rating = 4.6f,
            description = "Hair spray profesional dengan aerosol partikel mikro yang mengunci gaya rambut Anda tanpa meninggalkan residu putih keras atau lengket. Tahan badai dan kelembapan ekstrem.",
            sizes = listOf("250ml", "400ml"),
            details = mapOf(
                "Hold" to "Freeze Lock (5/5)",
                "Shine" to "Natural Satin",
                "Aroma" to "Classic Vanilla Barber",
                "Ingredients" to "Vinyl Caprolactam, Vitamin E"
            )
        ),
        Product(
            id = "tonic-05",
            name = "Glow Roots Hair Tonic Vitalizer",
            category = Category.HAIR,
            subcategory = "Hairtonik",
            price = 110000,
            rating = 4.9f,
            description = "Formula revitalisasi kulit kepala yang merangsang pertumbuhan akar rambut, mengurangi kerontokan, serta memancarkan kilau sehat sepanjang hari. Sensasi mint sejuk menyegarkan kulit kepala.",
            sizes = listOf("150ml"),
            tag = "SOLUSI RAMBUT",
            details = mapOf(
                "Manfaat" to "Mencegah Kerontokan & Mempercepat Pertumbuhan",
                "Sensasi" to "Icy Cold Menthol Extra Refreshing",
                "Aroma" to "Spearmint & Ginger",
                "Ingredients" to "Ginseng Extract, Aloe Vera, Rosemary Oil"
            )
        ),
        Product(
            id = "shampoo-06",
            name = "Activated Charcoal Detox Shampoo",
            category = Category.HAIR,
            subcategory = "Shampo",
            price = 95000,
            rating = 4.8f,
            description = "Shampo premium dengan kandungan karbon aktif untuk menyerap sisa pomade, keringat, dan partikel polusi hingga ke pori-pori terdalam rambut. Membuat rambut tebal, harum bebas apek.",
            sizes = listOf("250ml", "500ml"),
            details = mapOf(
                "Fungsi" to "Deep Cleansing & Pomade Remover",
                "Aroma" to "Cold Pressed Peppermint & Tea Tree",
                "Ingredients" to "Activated Charcoal, Tea tree Oil, Biotin"
            )
        ),

        // === STREETWEAR FASHION ===
        Product(
            id = "tshirt-01",
            name = "KULTUR Heavyweight Typo Box Tee",
            category = Category.FASHION,
            subcategory = "T-shirt",
            price = 199000,
            rating = 5.0f,
            description = "Kaos streetwear berpotongan boxy oversized dengan ketebalan katun 240gsm Cotton Combed premium. Cetakan grafis sablon plastisol timbul eksklusif 'KULTUR' di dada kiri.",
            sizes = listOf("S", "M", "L", "XL"),
            tag = "MUST HAVE",
            details = mapOf(
                "Bahan" to "Heavy Cotton Combed 16s (240 gsm)",
                "Cutting" to "Boxy Oversized Premium Fit",
                "Sablon" to "High Density Plastisol Ink"
            )
        ),
        Product(
            id = "hoodie-02",
            name = "Signature Cyberpunk Warm Hoodie",
            category = Category.FASHION,
            subcategory = "Hoodie",
            price = 389000,
            rating = 4.9f,
            description = "Hoodie premium dengan bahan heavy French Terry 400gsm yang tebal dan lembut di dalam. Dilengkapi dengan hoodie double-lined besar tanpa tali untuk look minimalis futuristik.",
            sizes = listOf("M", "L", "XL", "XXL"),
            tag = "LIMITED",
            details = mapOf(
                "Bahan" to "Heavy French Terry 100% Cotton 400gsm",
                "Fit" to "Aesthetic Drop Shoulder Slim-Cuff",
                "Detail" to "Kantung Kangguru Tersembunyi"
            )
        ),
        Product(
            id = "crewneck-03",
            name = "Distressed Raw Edge Crewneck Sweater",
            category = Category.FASHION,
            subcategory = "Crewneck",
            price = 325000,
            rating = 4.7f,
            description = "Crewneck modis berdesain raw-edge (ujung robek artistik) untuk menonjolkan estetika grunge modern. Hangat untuk bepergian malam maupun nongkrong kasual.",
            sizes = listOf("S", "M", "L", "XL"),
            details = mapOf(
                "Bahan" to "Cotton Fleece Super Soft (330 gsm)",
                "Style" to "Distressed Grunge Edge Aesthetics",
                "Warna" to "Washed Charcoal Grey & Midnight Black"
            )
        ),
        Product(
            id = "cardigan-04",
            name = "KULTUR Mohair Knit Cardigan",
            category = Category.FASHION,
            subcategory = "Cardigan",
            price = 450000,
            rating = 4.8f,
            description = "Cardigan rajut wool mohair bertekstur lembut dengan pola anyaman asimetris yang elegan. Dilengkapi kancing tanduk asli eksklusif untuk mempercantik sentuhan streetwear dapper modern.",
            sizes = listOf("M", "L"),
            tag = "PREMIUM",
            details = mapOf(
                "Bahan" to "Premium Chunky Wool-Mohair Blend",
                "Fit" to "Relaxed Drop Shoulder Knitwear",
                "Kancing" to "Genuine Horn Buttons"
            )
        ),
        Product(
            id = "pants-05",
            name = "Utility Parachute Elastic Pants",
            category = Category.FASHION,
            subcategory = "Celana",
            price = 289000,
            rating = 4.6f,
            description = "Celana kargo berpotongan lebar dengan material parasut ripstop tahan air dan angin. Memiliki tali serut elastis di pinggang dan pergelangan kaki untuk gaya siluet adjustable.",
            sizes = listOf("28-30 (S)", "31-33 (M)", "34-36 (L)"),
            details = mapOf(
                "Bahan" to "Ultra-lite Water Repellent Ripstop Nylon",
                "Fit" to "Loose Balloon-Parachute Fit",
                "Saku" to "6-Pocket Utility Compartments"
            )
        ),
        Product(
            id = "shoes-06",
            name = "Architech Low-Top Suede Sneakers",
            category = Category.FASHION,
            subcategory = "Sepatu",
            price = 799000,
            rating = 4.9f,
            description = "Sneakers bersiluet retro basket modern yang memadukan calfskin suede premium dan mesh bernapas. Dilengkapi sol karet gum vulkanisir super empuk untuk aktivitas urban tanpa henti.",
            sizes = listOf("40", "41", "42", "43", "44"),
            tag = "KADO TERBAIK",
            details = mapOf(
                "Luar" to "Genuine Roughout Calf Suede & Nylon Mesh",
                "Sol" to "Vulcanized Ortholite High-Rebound Insole",
                "Lacing" to "Flat Waxed Premium Laces"
            )
        ),
        Product(
            id = "accessories-07",
            name = "KULTUR Industrial Steel Chain Necklace",
            category = Category.FASHION,
            subcategory = "Accesories",
            price = 125000,
            rating = 4.8f,
            description = "Kalung rantai baja industri hypoallergenic grade 316L dengan liontin minimalis berukir barcode KULTUR. Tidak karatan, tidak luntur, awet dipakai bertahun-tahun.",
            sizes = listOf("55cm Standard"),
            details = mapOf(
                "Material" to "316L Industrial-grade Surgical Stainless Steel",
                "Lapisan" to "Anti-tarnish Protective Coat",
                "Liontin" to "Bar Emblem Barcode Engraved Pendent"
            )
        ),
        Product(
            id = "hat-08",
            name = "Faded Distressed Signature Beanie",
            category = Category.FASHION,
            subcategory = "Topi",
            price = 99000,
            rating = 4.7f,
            description = "Topi kupluk beanie rajut katun berpotongan pendek (fisherman style) dengan pencucian washed faded menciptakan kesan kumal nan modis. Sangat fleksibel mengikuti bentuk kepala.",
            sizes = listOf("All Size Fit"),
            details = mapOf(
                "Bahan" to "100% Acrylic Washed Cotton Thread",
                "Cutting" to "Short-Fit Fisherman Rolled Beanie",
                "Elastisitas" to "High-recovery Ribbed Knit"
            )
        ),
        Product(
            id = "glasses-09",
            name = "Phantom Shield UV-400 Glasses",
            category = Category.FASHION,
            subcategory = "Kacamata",
            price = 185000,
            rating = 4.9f,
            description = "Kacamata hitam bersiluet futuristik dengan proteksi layar UV-400 penuh. Gagang asetat tebal memberikan kenyamanan dan proteksi silau tingkat tinggi dengan estetika masa depan.",
            sizes = listOf("Satu Ukuran"),
            tag = "FUTURISTIK",
            details = mapOf(
                "Lensa" to "Polarized UV-400 Protective Scratch-Resistant",
                "Frame" to "Molded Black Cellulose Acetate",
                "Case" to "Hardened Synthetic Leather Case Included"
            )
        )
    )

    fun getProductById(id: String): Product? = sampleProducts.find { it.id == id }
}
