package org.edward.app.data.remote.product

import kotlinx.coroutines.delay
import org.edward.app.data.utils.AsyncResult

class MockProductRepositoryImpl : ProductRepository {

    // ── Shared option-value pools ────────────────────────────────────────

    private val sizeValues = mapOf(
        "xs" to OptionValue(id = "ov-xs", value = "XS", displayOrder = 1),
        "s" to OptionValue(id = "ov-s", value = "S", displayOrder = 2),
        "m" to OptionValue(id = "ov-m", value = "M", displayOrder = 3),
        "l" to OptionValue(id = "ov-l", value = "L", displayOrder = 4),
        "xl" to OptionValue(id = "ov-xl", value = "XL", displayOrder = 5),
        "xxl" to OptionValue(id = "ov-xxl", value = "XXL", displayOrder = 6),
    )

    private val pantsSize = mapOf(
        "28" to OptionValue(id = "ov-28", value = "28", displayOrder = 1),
        "30" to OptionValue(id = "ov-30", value = "30", displayOrder = 2),
        "32" to OptionValue(id = "ov-32", value = "32", displayOrder = 3),
        "34" to OptionValue(id = "ov-34", value = "34", displayOrder = 4),
        "36" to OptionValue(id = "ov-36", value = "36", displayOrder = 5),
    )

    private fun sizeOptionType(keys: List<String>, isPants: Boolean = false): OptionType {
        val src = if (isPants) pantsSize else sizeValues
        return OptionType(
            id = if (isPants) "ot-pants-size" else "ot-size",
            name = "Size",
            displayOrder = 1,
            optionValues = keys.mapNotNull { src[it] }
        )
    }

    private fun colorOptionType(id: String, colors: List<Pair<String, String>>): OptionType {
        return OptionType(
            id = id,
            name = "Color",
            displayOrder = 2,
            optionValues = colors.mapIndexed { i, (ovId, name) ->
                OptionValue(id = ovId, value = name, displayOrder = i + 1)
            }
        )
    }

    private fun optionType(id: String, name: String, values: List<Pair<String, String>>, order: Int = 1): OptionType {
        return OptionType(
            id = id,
            name = name,
            displayOrder = order,
            optionValues = values.mapIndexed { i, (ovId, label) ->
                OptionValue(id = ovId, value = label, displayOrder = i + 1)
            }
        )
    }

    // ── Product list (summary) ───────────────────────────────────────────

    private val mockProducts = listOf(
        // ── Fashion ──
        Product(
            id = "p-001", name = "AIRism Cotton Crew Neck T-Shirt",
            description = "Ultra-light, quick-drying fabric with a smooth cotton feel.",
            brand = "Essentials", minPrice = 14.90, maxPrice = 14.90,
            totalSaleCount = 8420, rating = 4.7,
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800",
                "https://images.unsplash.com/photo-1562157873-818bc0726f68?w=800",
            )
        ),
        Product(
            id = "p-002", name = "Ultra Stretch Slim-Fit Jeans",
            description = "Superior stretch denim that moves with you.",
            brand = "Bottoms", minPrice = 39.90, maxPrice = 39.90,
            totalSaleCount = 5312, rating = 4.5,
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1542272604-787c3835535d?w=800",
                "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800",
            )
        ),
        Product(
            id = "p-003", name = "Ultra Light Down Puffer Jacket",
            description = "Premium down insulation in an incredibly light package.",
            brand = "Outerwear", minPrice = 69.90, maxPrice = 79.90,
            totalSaleCount = 3890, rating = 4.8,
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1544923246-77307dd270cb?w=800",
                "https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=800",
            )
        ),
        Product(
            id = "p-004", name = "Oversized Sweat Hoodie",
            description = "Relaxed oversized fit with a soft brushed interior.",
            brand = "Loungewear", minPrice = 29.90, maxPrice = 34.90,
            totalSaleCount = 6745, rating = 4.6,
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800",
                "https://images.unsplash.com/photo-1620799140188-3b2a02fd9a77?w=800",
            )
        ),
        Product(
            id = "p-005", name = "Merino Blend Crew Neck Sweater",
            description = "Fine-gauge merino wool blend for warmth without bulk.",
            brand = "Knitwear", minPrice = 49.90, maxPrice = 49.90,
            totalSaleCount = 2134, rating = 4.4,
            mediaUrls = listOf("https://images.unsplash.com/photo-1434389677669-e08b4cda3a40?w=800")
        ),
        Product(
            id = "p-006", name = "Smart Ankle Pants",
            description = "Wrinkle-resistant stretch fabric. Perfect from office to weekend.",
            brand = "Bottoms", minPrice = 39.90, maxPrice = 39.90,
            totalSaleCount = 4567, rating = 4.3,
            mediaUrls = listOf("https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=800")
        ),
        Product(
            id = "p-007", name = "Linen Blend Short Sleeve Shirt",
            description = "Breathable linen-cotton blend for warm-weather comfort.",
            brand = "Shirts", minPrice = 29.90, maxPrice = 29.90,
            totalSaleCount = 1876, rating = 4.2,
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800",
                "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800",
            )
        ),
        Product(
            id = "p-008", name = "Dry-EX Crew Neck T-Shirt",
            description = "Advanced moisture-wicking fabric with mesh ventilation zones.",
            brand = "Sport Utility", minPrice = 19.90, maxPrice = 19.90,
            totalSaleCount = 7230, rating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=800")
        ),

        // ── Electronics ──
        Product(
            id = "p-009", name = "Wireless Noise-Cancelling Headphones",
            description = "Premium ANC headphones with 30-hour battery life.",
            brand = "Electronics", minPrice = 199.00, maxPrice = 249.00,
            totalSaleCount = 12340, rating = 4.8,
            mediaUrls = listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800")
        ),
        Product(
            id = "p-010", name = "Smart Fitness Watch Pro",
            description = "Advanced health monitoring with GPS and AMOLED display.",
            brand = "Electronics", minPrice = 149.00, maxPrice = 179.00,
            totalSaleCount = 9870, rating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800")
        ),
        Product(
            id = "p-011", name = "Portable Bluetooth Speaker",
            description = "Waterproof speaker with 360° sound and 20-hour playtime.",
            brand = "Electronics", minPrice = 59.90, maxPrice = 79.90,
            totalSaleCount = 6540, rating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800")
        ),
        Product(
            id = "p-012", name = "USB-C Fast Charging Cable",
            description = "Braided nylon cable with 100W power delivery. 2-meter length.",
            brand = "Electronics", minPrice = 12.90, maxPrice = 19.90,
            totalSaleCount = 23100, rating = 4.3,
            mediaUrls = listOf("https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800")
        ),
        Product(
            id = "p-013", name = "Wireless Earbuds Pro",
            description = "True wireless earbuds with hybrid ANC and spatial audio.",
            brand = "Electronics", minPrice = 129.00, maxPrice = 129.00,
            totalSaleCount = 15600, rating = 4.7,
            mediaUrls = listOf("https://images.unsplash.com/photo-1590658268037-6bf12f032f55?w=800")
        ),

        // ── Health & Beauty ──
        Product(
            id = "p-014", name = "Vitamin C Brightening Serum",
            description = "20% pure vitamin C with hyaluronic acid for radiant skin.",
            brand = "Beauty", minPrice = 24.90, maxPrice = 24.90,
            totalSaleCount = 8940, rating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800")
        ),
        Product(
            id = "p-015", name = "Organic Green Tea Matcha Powder",
            description = "Ceremonial grade matcha sourced from Uji, Kyoto.",
            brand = "Health", minPrice = 29.90, maxPrice = 49.90,
            totalSaleCount = 4320, rating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1515823064-d6e0c04616a7?w=800")
        ),
        Product(
            id = "p-016", name = "Resistance Bands Set",
            description = "5-level latex-free exercise bands for home workouts.",
            brand = "Fitness", minPrice = 19.90, maxPrice = 19.90,
            totalSaleCount = 11200, rating = 4.4,
            mediaUrls = listOf("https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=800")
        ),
        Product(
            id = "p-017", name = "Retinol Night Cream",
            description = "Advanced retinol formula with peptides for overnight renewal.",
            brand = "Beauty", minPrice = 34.90, maxPrice = 34.90,
            totalSaleCount = 6780, rating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=800")
        ),
        Product(
            id = "p-018", name = "Protein Whey Isolate",
            description = "30g protein per serving. Low carb, lactose-free formula.",
            brand = "Health", minPrice = 39.90, maxPrice = 59.90,
            totalSaleCount = 7850, rating = 4.3,
            mediaUrls = listOf("https://images.unsplash.com/photo-1593095948071-474c5cc2c989?w=800")
        ),

        // ── Home & Living ──
        Product(
            id = "p-019", name = "Soy Wax Scented Candle",
            description = "Hand-poured soy candle with essential oils. 60-hour burn time.",
            brand = "Home", minPrice = 18.90, maxPrice = 24.90,
            totalSaleCount = 5640, rating = 4.7,
            mediaUrls = listOf("https://images.unsplash.com/photo-1602607663818-eb5e7ddf5a36?w=800")
        ),
        Product(
            id = "p-020", name = "Stainless Steel Insulated Bottle",
            description = "Double-wall vacuum insulated. Keeps drinks cold 24h or hot 12h.",
            brand = "Home", minPrice = 24.90, maxPrice = 29.90,
            totalSaleCount = 13400, rating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800")
        ),
        Product(
            id = "p-021", name = "Memory Foam Pillow",
            description = "Ergonomic contour design with cooling gel layer.",
            brand = "Home", minPrice = 39.90, maxPrice = 49.90,
            totalSaleCount = 4230, rating = 4.4,
            mediaUrls = listOf("https://images.unsplash.com/photo-1592789705501-f9ae4278a9c9?w=800")
        ),
        Product(
            id = "p-022", name = "Bamboo Cutting Board Set",
            description = "3-piece set with juice grooves and easy-grip handles.",
            brand = "Home", minPrice = 22.90, maxPrice = 22.90,
            totalSaleCount = 3150, rating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1594226801341-41427b4e5c22?w=800")
        ),

        // ── Sports & Outdoor ──
        Product(
            id = "p-023", name = "Ultralight Running Shoes",
            description = "Carbon-plate midsole with breathable knit upper. 195g per shoe.",
            brand = "Sports", minPrice = 89.90, maxPrice = 119.90,
            totalSaleCount = 6780, rating = 4.7,
            mediaUrls = listOf("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800")
        ),
        Product(
            id = "p-024", name = "Yoga Mat Premium",
            description = "6mm thick non-slip TPE mat with alignment markers.",
            brand = "Sports", minPrice = 34.90, maxPrice = 44.90,
            totalSaleCount = 8920, rating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800")
        ),
        Product(
            id = "p-025", name = "Hiking Backpack 40L",
            description = "Water-resistant ripstop nylon with ventilated back panel.",
            brand = "Outdoor", minPrice = 59.90, maxPrice = 69.90,
            totalSaleCount = 3450, rating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800")
        ),
        Product(
            id = "p-026", name = "Polarized Sport Sunglasses",
            description = "TR90 frame with UV400 polarized lenses. Lightweight and durable.",
            brand = "Sports", minPrice = 29.90, maxPrice = 39.90,
            totalSaleCount = 5120, rating = 4.4,
            mediaUrls = listOf("https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=800")
        ),
    )

    // ── Product details ──────────────────────────────────────────────────

    private val mockProductDetails: Map<String, ProductDetail> = mapOf(
        // ── Fashion (p-001 … p-008) ──
        "p-001" to ProductDetail(
            id = "p-001", name = "AIRism Cotton Crew Neck T-Shirt",
            description = "Ultra-light, quick-drying fabric with a smooth cotton feel. Features AIRism technology that wicks moisture, neutralizes odors, and releases heat for comfort in any season.",
            brand = "Essentials", totalSaleCount = 8420, averageRating = 4.7,
            mediaUrls = listOf("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800", "https://images.unsplash.com/photo-1562157873-818bc0726f68?w=800"),
            optionTypes = listOf(
                sizeOptionType(listOf("xs", "s", "m", "l", "xl", "xxl")),
                colorOptionType("ot-c1", listOf("ov-c1-w" to "White", "ov-c1-b" to "Black", "ov-c1-g" to "Gray", "ov-c1-n" to "Navy")),
            ),
            variants = listOf(
                Variant(id = "v-001", sku = "AIR-W-S", price = 14.90, stock = 80, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-s"), SelectedOption("ot-c1", "ov-c1-w"))),
                Variant(id = "v-002", sku = "AIR-W-M", price = 14.90, stock = 120, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-m"), SelectedOption("ot-c1", "ov-c1-w"))),
                Variant(id = "v-003", sku = "AIR-B-M", price = 14.90, stock = 95, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-m"), SelectedOption("ot-c1", "ov-c1-b"))),
                Variant(id = "v-004", sku = "AIR-B-L", price = 14.90, stock = 65, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-l"), SelectedOption("ot-c1", "ov-c1-b"))),
                Variant(id = "v-005", sku = "AIR-G-XL", price = 14.90, stock = 40, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-xl"), SelectedOption("ot-c1", "ov-c1-g"))),
                Variant(id = "v-006", sku = "AIR-N-XXL", price = 14.90, stock = 0, status = "INACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-xxl"), SelectedOption("ot-c1", "ov-c1-n"))),
            )
        ),
        "p-002" to ProductDetail(
            id = "p-002", name = "Ultra Stretch Slim-Fit Jeans",
            description = "Superior stretch denim that moves with you throughout the day. Slim fit with a clean silhouette.",
            brand = "Bottoms", totalSaleCount = 5312, averageRating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1542272604-787c3835535d?w=800", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800"),
            optionTypes = listOf(
                sizeOptionType(listOf("28", "30", "32", "34", "36"), isPants = true),
                colorOptionType("ot-c2", listOf("ov-c2-ind" to "Indigo", "ov-c2-blk" to "Black", "ov-c2-blu" to "Blue")),
            ),
            variants = listOf(
                Variant(id = "v-010", sku = "JS-IND-30", price = 39.90, stock = 55, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-pants-size", "ov-30"), SelectedOption("ot-c2", "ov-c2-ind"))),
                Variant(id = "v-011", sku = "JS-IND-32", price = 39.90, stock = 70, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-pants-size", "ov-32"), SelectedOption("ot-c2", "ov-c2-ind"))),
                Variant(id = "v-012", sku = "JS-BLK-32", price = 39.90, stock = 45, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-pants-size", "ov-32"), SelectedOption("ot-c2", "ov-c2-blk"))),
                Variant(id = "v-013", sku = "JS-BLU-34", price = 39.90, stock = 30, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-pants-size", "ov-34"), SelectedOption("ot-c2", "ov-c2-blu"))),
            )
        ),
        "p-003" to ProductDetail(
            id = "p-003", name = "Ultra Light Down Puffer Jacket",
            description = "Premium 90% down insulation in an incredibly light package. Water-repellent outer shell.",
            brand = "Outerwear", totalSaleCount = 3890, averageRating = 4.8,
            mediaUrls = listOf("https://images.unsplash.com/photo-1544923246-77307dd270cb?w=800", "https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=800"),
            optionTypes = listOf(
                sizeOptionType(listOf("s", "m", "l", "xl")),
                colorOptionType("ot-c3", listOf("ov-c3-blk" to "Black", "ov-c3-nvy" to "Navy", "ov-c3-olv" to "Olive")),
            ),
            variants = listOf(
                Variant(id = "v-020", sku = "ULD-BLK-S", price = 79.90, salePrice = 69.90, stock = 20, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-s"), SelectedOption("ot-c3", "ov-c3-blk"))),
                Variant(id = "v-021", sku = "ULD-BLK-M", price = 79.90, salePrice = 69.90, stock = 35, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-m"), SelectedOption("ot-c3", "ov-c3-blk"))),
                Variant(id = "v-022", sku = "ULD-NVY-L", price = 79.90, stock = 25, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-l"), SelectedOption("ot-c3", "ov-c3-nvy"))),
                Variant(id = "v-023", sku = "ULD-OLV-XL", price = 79.90, stock = 10, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-xl"), SelectedOption("ot-c3", "ov-c3-olv"))),
            )
        ),
        "p-004" to ProductDetail(
            id = "p-004", name = "Oversized Sweat Hoodie",
            description = "Relaxed oversized fit with a soft brushed interior. Kangaroo pocket and adjustable drawstring hood.",
            brand = "Loungewear", totalSaleCount = 6745, averageRating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800", "https://images.unsplash.com/photo-1620799140188-3b2a02fd9a77?w=800"),
            optionTypes = listOf(
                sizeOptionType(listOf("s", "m", "l", "xl", "xxl")),
                colorOptionType("ot-c4", listOf("ov-c4-blk" to "Black", "ov-c4-gry" to "Dark Gray", "ov-c4-crm" to "Cream", "ov-c4-grn" to "Sage Green")),
            ),
            variants = listOf(
                Variant(id = "v-030", sku = "HD-BLK-M", price = 34.90, salePrice = 29.90, stock = 50, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-m"), SelectedOption("ot-c4", "ov-c4-blk"))),
                Variant(id = "v-031", sku = "HD-GRY-L", price = 34.90, stock = 40, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-l"), SelectedOption("ot-c4", "ov-c4-gry"))),
                Variant(id = "v-032", sku = "HD-CRM-M", price = 29.90, stock = 60, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-m"), SelectedOption("ot-c4", "ov-c4-crm"))),
                Variant(id = "v-033", sku = "HD-GRN-XL", price = 34.90, stock = 15, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-xl"), SelectedOption("ot-c4", "ov-c4-grn"))),
                Variant(id = "v-034", sku = "HD-BLK-XXL", price = 34.90, stock = 0, status = "INACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-xxl"), SelectedOption("ot-c4", "ov-c4-blk"))),
            )
        ),
        "p-005" to ProductDetail(
            id = "p-005", name = "Merino Blend Crew Neck Sweater",
            description = "Fine-gauge merino wool blend knit for natural warmth without bulk. Machine washable.",
            brand = "Knitwear", totalSaleCount = 2134, averageRating = 4.4,
            mediaUrls = listOf("https://images.unsplash.com/photo-1434389677669-e08b4cda3a40?w=800"),
            optionTypes = listOf(
                sizeOptionType(listOf("s", "m", "l", "xl")),
                colorOptionType("ot-c5", listOf("ov-c5-cmn" to "Camel", "ov-c5-dgr" to "Dark Gray", "ov-c5-wnr" to "Wine Red")),
            ),
            variants = listOf(
                Variant(id = "v-040", sku = "MR-CMN-M", price = 49.90, stock = 28, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-m"), SelectedOption("ot-c5", "ov-c5-cmn"))),
                Variant(id = "v-041", sku = "MR-DGR-L", price = 49.90, stock = 22, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-l"), SelectedOption("ot-c5", "ov-c5-dgr"))),
                Variant(id = "v-042", sku = "MR-WNR-S", price = 49.90, stock = 18, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-s"), SelectedOption("ot-c5", "ov-c5-wnr"))),
            )
        ),
        "p-006" to ProductDetail(
            id = "p-006", name = "Smart Ankle Pants",
            description = "Wrinkle-resistant stretch fabric with a two-way stretch. Ankle-length cut.",
            brand = "Bottoms", totalSaleCount = 4567, averageRating = 4.3,
            mediaUrls = listOf("https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=800"),
            optionTypes = listOf(
                sizeOptionType(listOf("28", "30", "32", "34", "36"), isPants = true),
                colorOptionType("ot-c6", listOf("ov-c6-blk" to "Black", "ov-c6-nvy" to "Navy", "ov-c6-gry" to "Gray")),
            ),
            variants = listOf(
                Variant(id = "v-050", sku = "SP-BLK-30", price = 39.90, stock = 42, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-pants-size", "ov-30"), SelectedOption("ot-c6", "ov-c6-blk"))),
                Variant(id = "v-051", sku = "SP-NVY-32", price = 39.90, stock = 38, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-pants-size", "ov-32"), SelectedOption("ot-c6", "ov-c6-nvy"))),
                Variant(id = "v-052", sku = "SP-GRY-34", price = 39.90, stock = 25, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-pants-size", "ov-34"), SelectedOption("ot-c6", "ov-c6-gry"))),
            )
        ),
        "p-007" to ProductDetail(
            id = "p-007", name = "Linen Blend Short Sleeve Shirt",
            description = "Breathable linen-cotton blend for warm-weather comfort. Relaxed fit with camp collar.",
            brand = "Shirts", totalSaleCount = 1876, averageRating = 4.2,
            mediaUrls = listOf("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800"),
            optionTypes = listOf(
                sizeOptionType(listOf("s", "m", "l", "xl")),
                colorOptionType("ot-c7", listOf("ov-c7-wht" to "Off White", "ov-c7-blu" to "Light Blue", "ov-c7-bge" to "Beige")),
            ),
            variants = listOf(
                Variant(id = "v-060", sku = "LN-WHT-M", price = 29.90, stock = 30, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-m"), SelectedOption("ot-c7", "ov-c7-wht"))),
                Variant(id = "v-061", sku = "LN-BLU-L", price = 29.90, stock = 25, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-l"), SelectedOption("ot-c7", "ov-c7-blu"))),
                Variant(id = "v-062", sku = "LN-BGE-S", price = 29.90, stock = 20, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-s"), SelectedOption("ot-c7", "ov-c7-bge"))),
            )
        ),
        "p-008" to ProductDetail(
            id = "p-008", name = "Dry-EX Crew Neck T-Shirt",
            description = "Advanced moisture-wicking fabric with mesh ventilation zones for enhanced airflow.",
            brand = "Sport Utility", totalSaleCount = 7230, averageRating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=800"),
            optionTypes = listOf(
                sizeOptionType(listOf("s", "m", "l", "xl", "xxl")),
                colorOptionType("ot-c8", listOf("ov-c8-blk" to "Black", "ov-c8-wht" to "White", "ov-c8-nvy" to "Navy")),
            ),
            variants = listOf(
                Variant(id = "v-070", sku = "DRY-BLK-M", price = 19.90, stock = 90, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-m"), SelectedOption("ot-c8", "ov-c8-blk"))),
                Variant(id = "v-071", sku = "DRY-WHT-L", price = 19.90, stock = 75, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-l"), SelectedOption("ot-c8", "ov-c8-wht"))),
                Variant(id = "v-072", sku = "DRY-NVY-XL", price = 19.90, stock = 50, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-xl"), SelectedOption("ot-c8", "ov-c8-nvy"))),
                Variant(id = "v-073", sku = "DRY-BLK-XXL", price = 19.90, stock = 0, status = "INACTIVE", selectedOptions = listOf(SelectedOption("ot-size", "ov-xxl"), SelectedOption("ot-c8", "ov-c8-blk"))),
            )
        ),

        // ── Electronics ──
        "p-009" to ProductDetail(
            id = "p-009", name = "Wireless Noise-Cancelling Headphones",
            description = "Premium ANC with adaptive transparency mode. 30-hour battery, Hi-Res audio certified, multipoint connection.",
            brand = "Electronics", totalSaleCount = 12340, averageRating = 4.8,
            mediaUrls = listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800"),
            optionTypes = listOf(
                colorOptionType("ot-c9", listOf("ov-c9-blk" to "Black", "ov-c9-slv" to "Silver", "ov-c9-mid" to "Midnight Blue")),
            ),
            variants = listOf(
                Variant(id = "v-080", sku = "HP-BLK", price = 249.00, salePrice = 199.00, stock = 45, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c9", "ov-c9-blk"))),
                Variant(id = "v-081", sku = "HP-SLV", price = 249.00, salePrice = 199.00, stock = 30, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c9", "ov-c9-slv"))),
                Variant(id = "v-082", sku = "HP-MID", price = 249.00, stock = 15, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c9", "ov-c9-mid"))),
            )
        ),
        "p-010" to ProductDetail(
            id = "p-010", name = "Smart Fitness Watch Pro",
            description = "1.4\" AMOLED display, built-in GPS, heart rate & SpO2 monitoring, sleep tracking, 100+ workout modes. 14-day battery life.",
            brand = "Electronics", totalSaleCount = 9870, averageRating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800"),
            optionTypes = listOf(
                optionType("ot-band", "Band Size", listOf("ov-band-s" to "S (130-175mm)", "ov-band-l" to "L (165-210mm)")),
                colorOptionType("ot-c10", listOf("ov-c10-blk" to "Obsidian", "ov-c10-grn" to "Forest Green", "ov-c10-org" to "Sunrise Orange")),
            ),
            variants = listOf(
                Variant(id = "v-090", sku = "FW-BLK-S", price = 179.00, salePrice = 149.00, stock = 60, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-band", "ov-band-s"), SelectedOption("ot-c10", "ov-c10-blk"))),
                Variant(id = "v-091", sku = "FW-BLK-L", price = 179.00, salePrice = 149.00, stock = 40, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-band", "ov-band-l"), SelectedOption("ot-c10", "ov-c10-blk"))),
                Variant(id = "v-092", sku = "FW-GRN-S", price = 179.00, stock = 25, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-band", "ov-band-s"), SelectedOption("ot-c10", "ov-c10-grn"))),
                Variant(id = "v-093", sku = "FW-ORG-L", price = 179.00, stock = 20, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-band", "ov-band-l"), SelectedOption("ot-c10", "ov-c10-org"))),
            )
        ),
        "p-011" to ProductDetail(
            id = "p-011", name = "Portable Bluetooth Speaker",
            description = "IP67 waterproof & dustproof, 360° stereo sound, built-in microphone, USB-C fast charging. Pairs with a second speaker for true stereo.",
            brand = "Electronics", totalSaleCount = 6540, averageRating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800"),
            optionTypes = listOf(
                colorOptionType("ot-c11", listOf("ov-c11-blk" to "Black", "ov-c11-blu" to "Ocean Blue", "ov-c11-red" to "Red", "ov-c11-grn" to "Green")),
            ),
            variants = listOf(
                Variant(id = "v-100", sku = "SPK-BLK", price = 79.90, salePrice = 59.90, stock = 70, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c11", "ov-c11-blk"))),
                Variant(id = "v-101", sku = "SPK-BLU", price = 79.90, salePrice = 59.90, stock = 45, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c11", "ov-c11-blu"))),
                Variant(id = "v-102", sku = "SPK-RED", price = 79.90, stock = 30, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c11", "ov-c11-red"))),
                Variant(id = "v-103", sku = "SPK-GRN", price = 79.90, stock = 0, status = "INACTIVE", selectedOptions = listOf(SelectedOption("ot-c11", "ov-c11-grn"))),
            )
        ),
        "p-012" to ProductDetail(
            id = "p-012", name = "USB-C Fast Charging Cable",
            description = "Braided nylon with 100W PD. MFi certified, data transfer up to 480Mbps. Available in 1m and 2m.",
            brand = "Electronics", totalSaleCount = 23100, averageRating = 4.3,
            mediaUrls = listOf("https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800"),
            optionTypes = listOf(
                optionType("ot-len", "Length", listOf("ov-len-1" to "1 meter", "ov-len-2" to "2 meters")),
                colorOptionType("ot-c12", listOf("ov-c12-blk" to "Black", "ov-c12-wht" to "White")),
            ),
            variants = listOf(
                Variant(id = "v-110", sku = "CAB-BLK-1M", price = 12.90, stock = 200, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-len", "ov-len-1"), SelectedOption("ot-c12", "ov-c12-blk"))),
                Variant(id = "v-111", sku = "CAB-BLK-2M", price = 19.90, stock = 150, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-len", "ov-len-2"), SelectedOption("ot-c12", "ov-c12-blk"))),
                Variant(id = "v-112", sku = "CAB-WHT-1M", price = 12.90, stock = 180, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-len", "ov-len-1"), SelectedOption("ot-c12", "ov-c12-wht"))),
                Variant(id = "v-113", sku = "CAB-WHT-2M", price = 19.90, stock = 120, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-len", "ov-len-2"), SelectedOption("ot-c12", "ov-c12-wht"))),
            )
        ),
        "p-013" to ProductDetail(
            id = "p-013", name = "Wireless Earbuds Pro",
            description = "Hybrid ANC up to -45dB, spatial audio with head tracking, 6-hour playback (30h with case). IPX5 sweat & water resistant.",
            brand = "Electronics", totalSaleCount = 15600, averageRating = 4.7,
            mediaUrls = listOf("https://images.unsplash.com/photo-1590658268037-6bf12f032f55?w=800"),
            optionTypes = listOf(
                colorOptionType("ot-c13", listOf("ov-c13-blk" to "Black", "ov-c13-wht" to "White", "ov-c13-lav" to "Lavender")),
            ),
            variants = listOf(
                Variant(id = "v-120", sku = "EB-BLK", price = 129.00, stock = 85, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c13", "ov-c13-blk"))),
                Variant(id = "v-121", sku = "EB-WHT", price = 129.00, stock = 60, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c13", "ov-c13-wht"))),
                Variant(id = "v-122", sku = "EB-LAV", price = 129.00, stock = 35, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c13", "ov-c13-lav"))),
            )
        ),

        // ── Health & Beauty ──
        "p-014" to ProductDetail(
            id = "p-014", name = "Vitamin C Brightening Serum",
            description = "20% L-ascorbic acid with hyaluronic acid and vitamin E. Brightens, firms, and protects against free radicals. Airless pump bottle for maximum potency.",
            brand = "Beauty", totalSaleCount = 8940, averageRating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800"),
            optionTypes = listOf(
                optionType("ot-vol14", "Volume", listOf("ov-vol14-30" to "30ml", "ov-vol14-50" to "50ml")),
            ),
            variants = listOf(
                Variant(id = "v-130", sku = "VCS-30", price = 24.90, stock = 120, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-vol14", "ov-vol14-30"))),
                Variant(id = "v-131", sku = "VCS-50", price = 39.90, stock = 80, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-vol14", "ov-vol14-50"))),
            )
        ),
        "p-015" to ProductDetail(
            id = "p-015", name = "Organic Green Tea Matcha Powder",
            description = "Ceremonial grade from Uji, Kyoto. Stone-ground for ultra-fine texture. Rich in antioxidants, L-theanine, and natural caffeine.",
            brand = "Health", totalSaleCount = 4320, averageRating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1515823064-d6e0c04616a7?w=800"),
            optionTypes = listOf(
                optionType("ot-wt15", "Weight", listOf("ov-wt15-30" to "30g Tin", "ov-wt15-100" to "100g Pouch", "ov-wt15-200" to "200g Pouch")),
            ),
            variants = listOf(
                Variant(id = "v-140", sku = "MT-30", price = 29.90, stock = 60, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-wt15", "ov-wt15-30"))),
                Variant(id = "v-141", sku = "MT-100", price = 39.90, stock = 45, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-wt15", "ov-wt15-100"))),
                Variant(id = "v-142", sku = "MT-200", price = 49.90, stock = 25, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-wt15", "ov-wt15-200"))),
            )
        ),
        "p-016" to ProductDetail(
            id = "p-016", name = "Resistance Bands Set",
            description = "5 latex-free TPE bands (X-Light to X-Heavy). Includes carry bag, door anchor, and exercise guide.",
            brand = "Fitness", totalSaleCount = 11200, averageRating = 4.4,
            mediaUrls = listOf("https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=800"),
            optionTypes = listOf(
                optionType("ot-set16", "Set", listOf("ov-set16-3" to "3-Band Starter", "ov-set16-5" to "5-Band Complete")),
            ),
            variants = listOf(
                Variant(id = "v-150", sku = "RB-3", price = 14.90, stock = 200, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-set16", "ov-set16-3"))),
                Variant(id = "v-151", sku = "RB-5", price = 19.90, stock = 150, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-set16", "ov-set16-5"))),
            )
        ),
        "p-017" to ProductDetail(
            id = "p-017", name = "Retinol Night Cream",
            description = "0.5% encapsulated retinol with ceramides and squalane. Gradual release minimizes irritation. Fragrance-free.",
            brand = "Beauty", totalSaleCount = 6780, averageRating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=800"),
            optionTypes = listOf(
                optionType("ot-vol17", "Size", listOf("ov-vol17-30" to "30ml", "ov-vol17-50" to "50ml")),
            ),
            variants = listOf(
                Variant(id = "v-160", sku = "RNC-30", price = 34.90, stock = 90, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-vol17", "ov-vol17-30"))),
                Variant(id = "v-161", sku = "RNC-50", price = 49.90, stock = 55, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-vol17", "ov-vol17-50"))),
            )
        ),
        "p-018" to ProductDetail(
            id = "p-018", name = "Protein Whey Isolate",
            description = "30g protein, 1g sugar per serving. Grass-fed, lactose-free. Available in multiple flavors.",
            brand = "Health", totalSaleCount = 7850, averageRating = 4.3,
            mediaUrls = listOf("https://images.unsplash.com/photo-1593095948071-474c5cc2c989?w=800"),
            optionTypes = listOf(
                optionType("ot-flv", "Flavor", listOf("ov-flv-cho" to "Chocolate", "ov-flv-van" to "Vanilla", "ov-flv-unf" to "Unflavored")),
                optionType("ot-wt18", "Size", listOf("ov-wt18-1" to "1 lb", "ov-wt18-2" to "2 lbs"), order = 2),
            ),
            variants = listOf(
                Variant(id = "v-170", sku = "WP-CHO-1", price = 39.90, stock = 80, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-flv", "ov-flv-cho"), SelectedOption("ot-wt18", "ov-wt18-1"))),
                Variant(id = "v-171", sku = "WP-CHO-2", price = 59.90, stock = 50, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-flv", "ov-flv-cho"), SelectedOption("ot-wt18", "ov-wt18-2"))),
                Variant(id = "v-172", sku = "WP-VAN-1", price = 39.90, stock = 65, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-flv", "ov-flv-van"), SelectedOption("ot-wt18", "ov-wt18-1"))),
                Variant(id = "v-173", sku = "WP-VAN-2", price = 59.90, stock = 35, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-flv", "ov-flv-van"), SelectedOption("ot-wt18", "ov-wt18-2"))),
                Variant(id = "v-174", sku = "WP-UNF-2", price = 54.90, stock = 40, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-flv", "ov-flv-unf"), SelectedOption("ot-wt18", "ov-wt18-2"))),
            )
        ),

        // ── Home & Living ──
        "p-019" to ProductDetail(
            id = "p-019", name = "Soy Wax Scented Candle",
            description = "Hand-poured soy candle with essential oils. Cotton wick for a clean burn. Glass jar with bamboo lid.",
            brand = "Home", totalSaleCount = 5640, averageRating = 4.7,
            mediaUrls = listOf("https://images.unsplash.com/photo-1602607663818-eb5e7ddf5a36?w=800"),
            optionTypes = listOf(
                optionType("ot-scent", "Scent", listOf("ov-sc-lav" to "Lavender", "ov-sc-van" to "Vanilla Bean", "ov-sc-ced" to "Cedarwood", "ov-sc-cit" to "Citrus Burst")),
                optionType("ot-candle-sz", "Size", listOf("ov-csz-s" to "Small (4 oz)", "ov-csz-l" to "Large (8 oz)"), order = 2),
            ),
            variants = listOf(
                Variant(id = "v-180", sku = "CAN-LAV-S", price = 18.90, stock = 50, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-scent", "ov-sc-lav"), SelectedOption("ot-candle-sz", "ov-csz-s"))),
                Variant(id = "v-181", sku = "CAN-LAV-L", price = 24.90, stock = 35, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-scent", "ov-sc-lav"), SelectedOption("ot-candle-sz", "ov-csz-l"))),
                Variant(id = "v-182", sku = "CAN-VAN-S", price = 18.90, stock = 45, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-scent", "ov-sc-van"), SelectedOption("ot-candle-sz", "ov-csz-s"))),
                Variant(id = "v-183", sku = "CAN-VAN-L", price = 24.90, stock = 30, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-scent", "ov-sc-van"), SelectedOption("ot-candle-sz", "ov-csz-l"))),
                Variant(id = "v-184", sku = "CAN-CED-L", price = 24.90, stock = 20, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-scent", "ov-sc-ced"), SelectedOption("ot-candle-sz", "ov-csz-l"))),
                Variant(id = "v-185", sku = "CAN-CIT-S", price = 18.90, stock = 40, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-scent", "ov-sc-cit"), SelectedOption("ot-candle-sz", "ov-csz-s"))),
            )
        ),
        "p-020" to ProductDetail(
            id = "p-020", name = "Stainless Steel Insulated Bottle",
            description = "Double-wall vacuum insulated 18/8 stainless steel. BPA-free lid with carry loop. Available in 500ml and 750ml.",
            brand = "Home", totalSaleCount = 13400, averageRating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800"),
            optionTypes = listOf(
                optionType("ot-cap20", "Capacity", listOf("ov-cap-500" to "500ml", "ov-cap-750" to "750ml")),
                colorOptionType("ot-c20", listOf("ov-c20-blk" to "Matte Black", "ov-c20-wht" to "Arctic White", "ov-c20-grn" to "Sage")),
            ),
            variants = listOf(
                Variant(id = "v-190", sku = "BTL-BLK-500", price = 24.90, stock = 100, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-cap20", "ov-cap-500"), SelectedOption("ot-c20", "ov-c20-blk"))),
                Variant(id = "v-191", sku = "BTL-BLK-750", price = 29.90, stock = 70, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-cap20", "ov-cap-750"), SelectedOption("ot-c20", "ov-c20-blk"))),
                Variant(id = "v-192", sku = "BTL-WHT-500", price = 24.90, stock = 85, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-cap20", "ov-cap-500"), SelectedOption("ot-c20", "ov-c20-wht"))),
                Variant(id = "v-193", sku = "BTL-GRN-750", price = 29.90, stock = 45, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-cap20", "ov-cap-750"), SelectedOption("ot-c20", "ov-c20-grn"))),
            )
        ),
        "p-021" to ProductDetail(
            id = "p-021", name = "Memory Foam Pillow",
            description = "Ergonomic contour design with cooling gel layer. CertiPUR-US certified foam. Removable bamboo-viscose cover.",
            brand = "Home", totalSaleCount = 4230, averageRating = 4.4,
            mediaUrls = listOf("https://images.unsplash.com/photo-1592789705501-f9ae4278a9c9?w=800"),
            optionTypes = listOf(
                optionType("ot-firm", "Firmness", listOf("ov-firm-med" to "Medium", "ov-firm-firm" to "Firm")),
                optionType("ot-pillsz", "Size", listOf("ov-pillsz-std" to "Standard", "ov-pillsz-qn" to "Queen"), order = 2),
            ),
            variants = listOf(
                Variant(id = "v-200", sku = "MFP-MED-STD", price = 39.90, stock = 55, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-firm", "ov-firm-med"), SelectedOption("ot-pillsz", "ov-pillsz-std"))),
                Variant(id = "v-201", sku = "MFP-MED-QN", price = 49.90, stock = 35, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-firm", "ov-firm-med"), SelectedOption("ot-pillsz", "ov-pillsz-qn"))),
                Variant(id = "v-202", sku = "MFP-FRM-STD", price = 39.90, stock = 40, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-firm", "ov-firm-firm"), SelectedOption("ot-pillsz", "ov-pillsz-std"))),
                Variant(id = "v-203", sku = "MFP-FRM-QN", price = 49.90, stock = 25, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-firm", "ov-firm-firm"), SelectedOption("ot-pillsz", "ov-pillsz-qn"))),
            )
        ),
        "p-022" to ProductDetail(
            id = "p-022", name = "Bamboo Cutting Board Set",
            description = "3-piece set (small, medium, large) from sustainable Moso bamboo. Juice grooves, easy-grip handles. Hand wash recommended.",
            brand = "Home", totalSaleCount = 3150, averageRating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1594226801341-41427b4e5c22?w=800"),
            optionTypes = emptyList(),
            variants = listOf(
                Variant(id = "v-210", sku = "BCB-SET", price = 22.90, stock = 75, status = "ACTIVE", selectedOptions = emptyList()),
            )
        ),

        // ── Sports & Outdoor ──
        "p-023" to ProductDetail(
            id = "p-023", name = "Ultralight Running Shoes",
            description = "Carbon-plate midsole for energy return. Engineered knit upper with gusseted tongue. Weighs only 195g.",
            brand = "Sports", totalSaleCount = 6780, averageRating = 4.7,
            mediaUrls = listOf("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800"),
            optionTypes = listOf(
                optionType("ot-shoe", "Size", listOf("ov-sh-8" to "US 8", "ov-sh-9" to "US 9", "ov-sh-10" to "US 10", "ov-sh-11" to "US 11", "ov-sh-12" to "US 12")),
                colorOptionType("ot-c23", listOf("ov-c23-blk" to "Black/White", "ov-c23-red" to "Racing Red", "ov-c23-blu" to "Electric Blue")),
            ),
            variants = listOf(
                Variant(id = "v-220", sku = "RUN-BLK-9", price = 119.90, salePrice = 89.90, stock = 25, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-shoe", "ov-sh-9"), SelectedOption("ot-c23", "ov-c23-blk"))),
                Variant(id = "v-221", sku = "RUN-BLK-10", price = 119.90, salePrice = 89.90, stock = 30, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-shoe", "ov-sh-10"), SelectedOption("ot-c23", "ov-c23-blk"))),
                Variant(id = "v-222", sku = "RUN-RED-10", price = 119.90, stock = 20, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-shoe", "ov-sh-10"), SelectedOption("ot-c23", "ov-c23-red"))),
                Variant(id = "v-223", sku = "RUN-BLU-11", price = 119.90, stock = 15, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-shoe", "ov-sh-11"), SelectedOption("ot-c23", "ov-c23-blu"))),
                Variant(id = "v-224", sku = "RUN-BLK-12", price = 119.90, salePrice = 89.90, stock = 0, status = "INACTIVE", selectedOptions = listOf(SelectedOption("ot-shoe", "ov-sh-12"), SelectedOption("ot-c23", "ov-c23-blk"))),
            )
        ),
        "p-024" to ProductDetail(
            id = "p-024", name = "Yoga Mat Premium",
            description = "6mm thick TPE foam with laser-etched alignment markers. Non-slip on both sides. Includes carry strap.",
            brand = "Sports", totalSaleCount = 8920, averageRating = 4.6,
            mediaUrls = listOf("https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800"),
            optionTypes = listOf(
                colorOptionType("ot-c24", listOf("ov-c24-blk" to "Charcoal", "ov-c24-pur" to "Lavender", "ov-c24-grn" to "Sage Green", "ov-c24-blu" to "Ocean")),
                optionType("ot-thick", "Thickness", listOf("ov-th-4" to "4mm Travel", "ov-th-6" to "6mm Standard"), order = 2),
            ),
            variants = listOf(
                Variant(id = "v-230", sku = "YM-BLK-6", price = 44.90, stock = 50, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c24", "ov-c24-blk"), SelectedOption("ot-thick", "ov-th-6"))),
                Variant(id = "v-231", sku = "YM-PUR-6", price = 44.90, stock = 40, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c24", "ov-c24-pur"), SelectedOption("ot-thick", "ov-th-6"))),
                Variant(id = "v-232", sku = "YM-GRN-4", price = 34.90, stock = 30, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c24", "ov-c24-grn"), SelectedOption("ot-thick", "ov-th-4"))),
                Variant(id = "v-233", sku = "YM-BLU-6", price = 44.90, stock = 20, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c24", "ov-c24-blu"), SelectedOption("ot-thick", "ov-th-6"))),
            )
        ),
        "p-025" to ProductDetail(
            id = "p-025", name = "Hiking Backpack 40L",
            description = "Water-resistant ripstop nylon with ventilated mesh back panel. Rain cover included. Hydration-compatible.",
            brand = "Outdoor", totalSaleCount = 3450, averageRating = 4.5,
            mediaUrls = listOf("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800"),
            optionTypes = listOf(
                colorOptionType("ot-c25", listOf("ov-c25-blk" to "Black", "ov-c25-grn" to "Forest Green", "ov-c25-orn" to "Burnt Orange")),
                optionType("ot-bksz", "Size", listOf("ov-bksz-s" to "S/M (15\"-17\")", "ov-bksz-l" to "L/XL (18\"-20\")"), order = 2),
            ),
            variants = listOf(
                Variant(id = "v-240", sku = "HBP-BLK-SM", price = 69.90, salePrice = 59.90, stock = 25, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c25", "ov-c25-blk"), SelectedOption("ot-bksz", "ov-bksz-s"))),
                Variant(id = "v-241", sku = "HBP-BLK-LXL", price = 69.90, salePrice = 59.90, stock = 20, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c25", "ov-c25-blk"), SelectedOption("ot-bksz", "ov-bksz-l"))),
                Variant(id = "v-242", sku = "HBP-GRN-SM", price = 69.90, stock = 15, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c25", "ov-c25-grn"), SelectedOption("ot-bksz", "ov-bksz-s"))),
                Variant(id = "v-243", sku = "HBP-ORN-LXL", price = 69.90, stock = 10, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-c25", "ov-c25-orn"), SelectedOption("ot-bksz", "ov-bksz-l"))),
            )
        ),
        "p-026" to ProductDetail(
            id = "p-026", name = "Polarized Sport Sunglasses",
            description = "Ultra-flexible TR90 frame with rubber nose pads. UV400 polarized TAC lenses. Includes hard case and cleaning cloth.",
            brand = "Sports", totalSaleCount = 5120, averageRating = 4.4,
            mediaUrls = listOf("https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=800"),
            optionTypes = listOf(
                optionType("ot-lens", "Lens", listOf("ov-lens-gry" to "Gray", "ov-lens-brn" to "Brown", "ov-lens-mir" to "Blue Mirror")),
                optionType("ot-frm", "Frame", listOf("ov-frm-blk" to "Matte Black", "ov-frm-trt" to "Tortoise"), order = 2),
            ),
            variants = listOf(
                Variant(id = "v-250", sku = "SG-GRY-BLK", price = 29.90, stock = 60, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-lens", "ov-lens-gry"), SelectedOption("ot-frm", "ov-frm-blk"))),
                Variant(id = "v-251", sku = "SG-BRN-TRT", price = 34.90, stock = 40, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-lens", "ov-lens-brn"), SelectedOption("ot-frm", "ov-frm-trt"))),
                Variant(id = "v-252", sku = "SG-MIR-BLK", price = 39.90, stock = 35, status = "ACTIVE", selectedOptions = listOf(SelectedOption("ot-lens", "ov-lens-mir"), SelectedOption("ot-frm", "ov-frm-blk"))),
            )
        ),
    )

    // ── Repository methods ───────────────────────────────────────────────

    override suspend fun getProducts(skip: Int, take: Int): AsyncResult<List<Product>> {
        delay(if (skip == 0) 600L else 400L)
        val page = mockProducts.drop(skip).take(take)
        return AsyncResult.Success(page)
    }

    override suspend fun getProduct(id: String): AsyncResult<ProductDetail> {
        delay(500)
        val detail = mockProductDetails[id]
            ?: return AsyncResult.Error(displayMessage = "Product not found")
        return AsyncResult.Success(detail)
    }

    override suspend fun createProduct(request: CreateProductRequest): AsyncResult<CreateProductResponse> {
        delay(700)
        return AsyncResult.Success(CreateProductResponse(id = "p-new-${mockProducts.size + 1}"))
    }

    override suspend fun createOptionType(productId: String, request: CreateProductOptionTypeRequest): AsyncResult<String> {
        delay(400)
        return AsyncResult.Success("ot-new-${(100..999).random()}")
    }

    override suspend fun getOptionTypes(productId: String): AsyncResult<List<OptionType>> {
        delay(400)
        val detail = mockProductDetails[productId]
        return AsyncResult.Success(detail?.optionTypes ?: emptyList())
    }

    override suspend fun createOptionValue(optionTypeId: String, request: CreateProductOptionValueRequest): AsyncResult<String> {
        delay(400)
        return AsyncResult.Success("ov-new-${(100..999).random()}")
    }

    override suspend fun getOptionValues(optionTypeId: String): AsyncResult<List<OptionValue>> {
        delay(400)
        val values = mockProductDetails.values
            .flatMap { it.optionTypes }
            .firstOrNull { it.id == optionTypeId }
            ?.optionValues ?: emptyList()
        return AsyncResult.Success(values)
    }

    override suspend fun createVariant(productId: String, request: CreateProductVariantRequest): AsyncResult<String> {
        delay(500)
        return AsyncResult.Success("v-new-${(100..999).random()}")
    }
}
