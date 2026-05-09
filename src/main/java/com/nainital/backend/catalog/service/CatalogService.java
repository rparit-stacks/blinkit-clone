package com.nainital.backend.catalog.service;

import com.nainital.backend.catalog.dto.*;
import com.nainital.backend.catalog.model.*;
import com.nainital.backend.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final StoreRepository storeRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final BannerRepository bannerRepo;

    // ─── Categories ───────────────────────────────────────────────────────────

    public List<CategoryDto> getCategories(StoreCategory sc) {
        return categoryRepo.findAllByStoreCategoryAndActiveTrueOrderBySortOrderAsc(sc)
                .stream()
                .map(c -> new CategoryDto(c.getSlug(), c.getLabel(), c.getIcon()))
                .toList();
    }

    // ─── Stores / Restaurants ─────────────────────────────────────────────────

    public List<StoreDto> getStores(StoreCategory sc) {
        return storeRepo.findAllByStoreCategoryAndActiveTrueOrderBySortOrderAsc(sc)
                .stream().map(this::toStoreDto).toList();
    }

    public Optional<StoreDto> getStore(String id) {
        return storeRepo.findById(id).map(this::toStoreDto);
    }

    // ─── Products ─────────────────────────────────────────────────────────────

    public List<ProductDto> getProducts(StoreCategory sc, String categorySlug) {
        List<Product> list;
        if (categorySlug == null || categorySlug.equals("all")) {
            list = productRepo.findAllByStoreCategoryAndAvailableTrueOrderBySortOrderAsc(sc);
        } else {
            list = productRepo.findAllByStoreCategoryAndCategorySlugAndAvailableTrueOrderBySortOrderAsc(sc, categorySlug);
        }
        return list.stream().filter(this::isApprovedForCatalog).map(this::toProductDto).toList();
    }

    public List<ProductDto> getProductsByStore(String storeId) {
        return productRepo.findAllByStoreIdAndAvailableTrueOrderBySortOrderAsc(storeId)
                .stream().filter(this::isApprovedForCatalog).map(this::toProductDto).toList();
    }

    public List<ProductDto> getProductsByRestaurant(String restaurantId) {
        return productRepo.findAllByRestaurantIdAndAvailableTrueOrderBySortOrderAsc(restaurantId)
                .stream().filter(this::isApprovedForCatalog).map(this::toProductDto).toList();
    }

    public Optional<ProductDto> getProduct(String id) {
        return productRepo.findById(id).filter(this::isApprovedForCatalog).map(this::toProductDto);
    }

    public List<ProductDto> searchProducts(StoreCategory sc, String query) {
        return productRepo.findAllByStoreCategoryAndNameContainingIgnoreCaseAndAvailableTrue(sc, query)
                .stream().filter(this::isApprovedForCatalog).map(this::toProductDto).toList();
    }

    /** {@code approved == false} hides from customer catalog; null/true behaves as approved (legacy docs). */
    private boolean isApprovedForCatalog(Product p) {
        return !Boolean.FALSE.equals(p.getApproved());
    }

    // ─── Banners ──────────────────────────────────────────────────────────────

    public List<BannerDto> getBanners(StoreCategory sc) {
        return bannerRepo.findAllByStoreCategoryAndActiveTrueOrderBySortOrderAsc(sc)
                .stream()
                .map(b -> new BannerDto(b.getId(), sc.name().toLowerCase(),
                        b.getTitle(), b.getSubtitle(), b.getCode(), b.getImageUrl()))
                .toList();
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private StoreDto toStoreDto(Store s) {
        return StoreDto.builder()
                .id(s.getId())
                .storeCategory(s.getStoreCategory().name().toLowerCase())
                .name(s.getName())
                .description(s.getDescription())
                .image(s.getImage())
                .coverImage(s.getCoverImage())
                .cuisineTypes(s.getCuisineTypes())
                .eta(s.getEta())
                .rating(s.getRating())
                .offer(s.getOffer())
                .build();
    }

    private ProductDto toProductDto(Product p) {
        return ProductDto.builder()
                .id(p.getId())
                .storeCategory(p.getStoreCategory().name().toLowerCase())
                .storeId(p.getStoreId())
                .categorySlug(p.getCategorySlug())
                .name(p.getName())
                .description(p.getDescription())
                .image(p.getImage())
                .price(p.getPrice())
                .originalPrice(p.getOriginalPrice())
                .unit(p.getUnit())
                .badge(p.getBadge())
                .rating(p.getRating())
                .available(p.isAvailable())
                .restaurantId(p.getRestaurantId())
                .build();
    }

    // ─── Admin / Seed ─────────────────────────────────────────────────────────

    public void seedAll() {
        seedCategories();
        seedStores();
        seedProducts();
        seedBanners();
    }

    private void seedCategories() {
        if (categoryRepo.count() > 0) return;

        // FOOD
        int i = 1;
        categoryRepo.saveAll(List.of(
                cat(StoreCategory.FOOD, "biryani", "Biryani", "bowl-rice", i++),
                cat(StoreCategory.FOOD, "pizza", "Pizza", "pizza-slice", i++),
                cat(StoreCategory.FOOD, "burger", "Burger", "burger", i++),
                cat(StoreCategory.FOOD, "chinese", "Chinese", "bowl-food", i++),
                cat(StoreCategory.FOOD, "dessert", "Desserts", "cake-candles", i++),
                cat(StoreCategory.FOOD, "thali", "Thali", "plate-wheat", i)
        ));

        // BAZAAR
        i = 1;
        categoryRepo.saveAll(List.of(
                cat(StoreCategory.BAZAAR, "fresh", "Vegetables", "carrot", i++),
                cat(StoreCategory.BAZAAR, "fruits", "Fruits", "apple-whole", i++),
                cat(StoreCategory.BAZAAR, "dairy", "Dairy", "cheese", i++),
                cat(StoreCategory.BAZAAR, "grocery", "Grocery", "basket-shopping", i++),
                cat(StoreCategory.BAZAAR, "spices", "Spices", "pepper-hot", i++),
                cat(StoreCategory.BAZAAR, "cleaning", "Cleaning", "broom", i)
        ));

        // ELECTRONIC
        i = 1;
        categoryRepo.saveAll(List.of(
                cat(StoreCategory.ELECTRONIC, "mobiles", "Mobiles", "mobile-screen", i++),
                cat(StoreCategory.ELECTRONIC, "laptops", "Laptops", "laptop", i++),
                cat(StoreCategory.ELECTRONIC, "audio", "Audio", "headphones", i++),
                cat(StoreCategory.ELECTRONIC, "tv", "TV", "tv", i++),
                cat(StoreCategory.ELECTRONIC, "accessories", "Accessories", "keyboard", i)
        ));
    }

    private void seedStores() {
        if (storeRepo.count() > 0) return;

        storeRepo.saveAll(List.of(
                Store.builder().storeCategory(StoreCategory.FOOD).name("Nawab Biryani House")
                        .cuisineTypes("Biryani, Mughlai, Sweets").eta("25-30 mins").rating(4.6)
                        .offer("40% OFF up to ₹120")
                        .image("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=900&h=500&fit=crop")
                        .coverImage("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=900&h=500&fit=crop")
                        .active(true).sortOrder(1).build(),
                Store.builder().storeCategory(StoreCategory.FOOD).name("Stone Oven Pizza")
                        .cuisineTypes("Pizza, Italian, Desserts").eta("30-35 mins").rating(4.4)
                        .offer("Free garlic bread")
                        .image("https://images.unsplash.com/photo-1559339352-11d035aa65de?w=900&h=500&fit=crop")
                        .coverImage("https://images.unsplash.com/photo-1559339352-11d035aa65de?w=900&h=500&fit=crop")
                        .active(true).sortOrder(2).build(),
                Store.builder().storeCategory(StoreCategory.FOOD).name("Burger Garage")
                        .cuisineTypes("Burgers, Wraps, Fast Food").eta("20-25 mins").rating(4.3)
                        .offer("Buy 1 Get 1 on combos")
                        .image("https://images.unsplash.com/photo-1552566626-52f8b828add9?w=900&h=500&fit=crop")
                        .coverImage("https://images.unsplash.com/photo-1552566626-52f8b828add9?w=900&h=500&fit=crop")
                        .active(true).sortOrder(3).build(),
                Store.builder().storeCategory(StoreCategory.FOOD).name("Wok & Thali Kitchen")
                        .cuisineTypes("Chinese, Thali, North Indian").eta("28-32 mins").rating(4.5)
                        .offer("Flat ₹75 OFF")
                        .image("https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=900&h=500&fit=crop")
                        .coverImage("https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=900&h=500&fit=crop")
                        .active(true).sortOrder(4).build(),
                Store.builder().storeCategory(StoreCategory.BAZAAR).name("Nainital Fresh Mart")
                        .description("Fresh vegetables, fruits and dairy delivered daily")
                        .image("https://images.unsplash.com/photo-1542838132-92c53300491e?w=900&h=500&fit=crop")
                        .active(true).sortOrder(1).build(),
                Store.builder().storeCategory(StoreCategory.BAZAAR).name("Hill Grocery Store")
                        .description("Spices, grocery and household essentials")
                        .image("https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=900&h=500&fit=crop")
                        .active(true).sortOrder(2).build(),
                Store.builder().storeCategory(StoreCategory.ELECTRONIC).name("TechZone Nainital")
                        .description("Mobiles, laptops and all electronics")
                        .image("https://images.unsplash.com/photo-1491933382434-500287f9b54b?w=900&h=500&fit=crop")
                        .active(true).sortOrder(1).build()
        ));
    }

    private void seedProducts() {
        if (productRepo.count() > 0) return;

        List<Store> stores = storeRepo.findAll();
        String r1 = storeId(stores, "Nawab Biryani House");
        String r2 = storeId(stores, "Stone Oven Pizza");
        String r3 = storeId(stores, "Burger Garage");
        String r4 = storeId(stores, "Wok & Thali Kitchen");
        String bazaar1 = storeId(stores, "Nainital Fresh Mart");
        String bazaar2 = storeId(stores, "Hill Grocery Store");
        String tech1 = storeId(stores, "TechZone Nainital");

        productRepo.saveAll(List.of(
                // ── FOOD ──
                product(StoreCategory.FOOD, r1, r1, "biryani", "Chicken Biryani Family Pack",
                        "Aromatic basmati rice cooked with tender chicken pieces, infused with traditional spices. Serves 4.",
                        "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=300&h=300&fit=crop",
                        449, 599, "1.2 kg", "BEST SELLER", 4.5, 1),
                product(StoreCategory.FOOD, r2, r2, "pizza", "Margherita Pizza Large",
                        "Classic Italian pizza with fresh mozzarella, tomato sauce and basil on a crispy thin crust.",
                        "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=300&h=300&fit=crop",
                        299, 499, "450 g", null, 4.3, 2),
                product(StoreCategory.FOOD, r3, r3, "burger", "Classic Smash Burger",
                        "Juicy double-smashed beef patty with cheese, pickles, onions and special sauce.",
                        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300&h=300&fit=crop",
                        219, 349, "350 g", null, 4.1, 3),
                product(StoreCategory.FOOD, r4, r4, "chinese", "Hakka Noodles",
                        "Stir-fried noodles with fresh vegetables and Indo-Chinese spices.",
                        "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=300&h=300&fit=crop",
                        179, 249, "400 g", null, 4.0, 4),
                product(StoreCategory.FOOD, r1, r1, "dessert", "Gulab Jamun Pack",
                        "Soft, melt-in-mouth gulab jamuns soaked in aromatic sugar syrup.",
                        "https://images.unsplash.com/photo-1666190059469-570b92f4dbbb?w=300&h=300&fit=crop",
                        149, 199, "12 pcs", null, 4.6, 5),
                product(StoreCategory.FOOD, r3, r3, "burger", "Paneer Tikka Wrap",
                        "Grilled paneer tikka wrapped in a soft tortilla with mint chutney and veggies.",
                        "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=300&h=300&fit=crop",
                        169, 229, "280 g", null, 4.2, 6),
                product(StoreCategory.FOOD, r4, r4, "thali", "Veg Thali Special",
                        "Complete meal with dal, sabzi, roti, rice, raita and pickle.",
                        "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300&h=300&fit=crop",
                        199, 299, "Full Plate", "NEW", 4.4, 7),
                product(StoreCategory.FOOD, r2, r2, "dessert", "Chocolate Brownie",
                        "Rich, fudgy chocolate brownie with a gooey center.",
                        "https://images.unsplash.com/photo-1564355808539-22fda35bed7e?w=300&h=300&fit=crop",
                        129, 179, "200 g", null, 4.7, 8),

                // ── BAZAAR ──
                product(StoreCategory.BAZAAR, bazaar1, null, "fresh", "Fresh Tomatoes",
                        "Farm fresh red tomatoes, perfect for curries and salads.",
                        "https://images.unsplash.com/photo-1546470427-0d4db154ceb8?w=300&h=300&fit=crop",
                        40, 60, "1 kg", "FRESH", 4.4, 1),
                product(StoreCategory.BAZAAR, bazaar1, null, "dairy", "Farm Fresh Milk",
                        "Pure full cream milk from local dairy farms.",
                        "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=300&h=300&fit=crop",
                        68, 80, "1 litre", null, 4.7, 2),
                product(StoreCategory.BAZAAR, bazaar1, null, "fruits", "Organic Bananas",
                        "Naturally ripened organic bananas.",
                        "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=300&h=300&fit=crop",
                        50, 70, "1 dozen", null, 4.2, 3),
                product(StoreCategory.BAZAAR, bazaar2, null, "grocery", "Basmati Rice Premium",
                        "Long grain premium basmati rice, aged for extra flavour.",
                        "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=300&h=300&fit=crop",
                        450, 599, "5 kg", null, 4.6, 4),
                product(StoreCategory.BAZAAR, bazaar2, null, "spices", "Red Chilli Powder",
                        "Pure Kashmiri red chilli powder for rich color and mild heat.",
                        "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=300&h=300&fit=crop",
                        85, 110, "200 g", null, 4.3, 5),
                product(StoreCategory.BAZAAR, bazaar2, null, "cleaning", "Surf Excel Detergent",
                        "Powerful stain removal detergent powder for all fabrics.",
                        "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=300&h=300&fit=crop",
                        199, 250, "1 kg", "POPULAR", 4.1, 6),
                product(StoreCategory.BAZAAR, bazaar1, null, "dairy", "Fresh Paneer",
                        "Soft and fresh cottage cheese, made daily.",
                        "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=300&h=300&fit=crop",
                        160, 200, "500 g", null, 4.5, 7),
                product(StoreCategory.BAZAAR, bazaar1, null, "fruits", "Green Apples",
                        "Crisp and tangy green apples from Shimla.",
                        "https://images.unsplash.com/photo-1569870499705-504209102861?w=300&h=300&fit=crop",
                        180, 220, "1 kg", null, 4.3, 8),

                // ── ELECTRONIC ──
                product(StoreCategory.ELECTRONIC, tech1, null, "mobiles", "iPhone 15 Pro Max",
                        "A17 Pro chip, 48MP camera system, titanium design, all-day battery life.",
                        "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=300&h=300&fit=crop",
                        134900, 159900, "256 GB", "TOP PICK", 4.8, 1),
                product(StoreCategory.ELECTRONIC, tech1, null, "laptops", "MacBook Air M2",
                        "Supercharged by M2, with up to 18 hours of battery life.",
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=300&h=300&fit=crop",
                        99900, 119900, "13.6 inch", null, 4.7, 2),
                product(StoreCategory.ELECTRONIC, tech1, null, "audio", "Sony WH-1000XM5",
                        "Industry-leading noise cancellation with exceptional sound quality.",
                        "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=300&h=300&fit=crop",
                        24990, 34990, "250 g", "BEST SELLER", 4.6, 3),
                product(StoreCategory.ELECTRONIC, tech1, null, "tv", "Samsung 55\" 4K Smart TV",
                        "Crystal 4K UHD display with smart hub and voice assistant.",
                        "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=300&h=300&fit=crop",
                        42990, 59990, "55 inch", null, 4.5, 4),
                product(StoreCategory.ELECTRONIC, tech1, null, "accessories", "Logitech MX Keys",
                        "Advanced wireless illuminated keyboard with smart backlighting.",
                        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=300&h=300&fit=crop",
                        8995, 12995, "810 g", null, 4.4, 5),
                product(StoreCategory.ELECTRONIC, tech1, null, "mobiles", "OnePlus 12",
                        "Snapdragon 8 Gen 3, Hasselblad camera, 100W charging.",
                        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=300&h=300&fit=crop",
                        54999, 64999, "128 GB", null, 4.5, 6)
        ));
    }

    private void seedBanners() {
        if (bannerRepo.count() > 0) return;
        bannerRepo.saveAll(List.of(
                Banner.builder().storeCategory(StoreCategory.FOOD).title("50% OFF on First Order!")
                        .subtitle("Use code: SWIFT50").code("SWIFT50").active(true).sortOrder(1).build(),
                Banner.builder().storeCategory(StoreCategory.FOOD).title("Free Delivery All Day!")
                        .subtitle("No minimum order needed").active(true).sortOrder(2).build(),
                Banner.builder().storeCategory(StoreCategory.BAZAAR).title("Fresh Sabzi at ₹1")
                        .subtitle("First item at just ₹1").code("BAZAAR1").active(true).sortOrder(1).build(),
                Banner.builder().storeCategory(StoreCategory.ELECTRONIC).title("Mega Electronic Sale!")
                        .subtitle("Up to 70% off on gadgets").active(true).sortOrder(1).build(),
                Banner.builder().storeCategory(StoreCategory.ELECTRONIC).title("Buy 2 Get 1 Free")
                        .subtitle("On all accessories").active(true).sortOrder(2).build()
        ));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ProductCategory cat(StoreCategory sc, String slug, String label, String icon, int order) {
        return ProductCategory.builder()
                .storeCategory(sc).slug(slug).label(label).icon(icon)
                .sortOrder(order).active(true).build();
    }

    private Product product(StoreCategory sc, String storeId, String restaurantId,
                            String catSlug, String name, String desc, String image,
                            int price, int originalPrice, String unit, String badge,
                            double rating, int order) {
        return Product.builder()
                .storeCategory(sc).storeId(storeId).restaurantId(restaurantId)
                .categorySlug(catSlug).name(name).description(desc).image(image)
                .price(price).originalPrice(originalPrice).unit(unit).badge(badge)
                .rating(rating).available(true).approved(true).sortOrder(order).build();
    }

    private String storeId(List<Store> stores, String name) {
        return stores.stream().filter(s -> s.getName().equals(name))
                .findFirst().map(Store::getId).orElse(null);
    }
}
