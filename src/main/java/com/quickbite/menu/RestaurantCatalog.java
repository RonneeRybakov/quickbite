package com.quickbite.menu;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Provides the participating restaurants and their menus. Uses sample data for the prototype. */
public class RestaurantCatalog {

    private final List<Restaurant> restaurants;

    public RestaurantCatalog() {
        this(sampleData());
    }

    public RestaurantCatalog(List<Restaurant> restaurants) {
        this.restaurants = restaurants.stream()
                .sorted(Comparator.comparing(Restaurant::name))
                .toList();
    }

    /** All restaurants, sorted by name. */
    public List<Restaurant> findAll() {
        return restaurants;
    }

    public Optional<Restaurant> findById(String id) {
        return restaurants.stream().filter(r -> r.id().equals(id)).findFirst();
    }

    private static List<Restaurant> sampleData() {
        return List.of(
                new Restaurant("pizza-palace", "Pizza Palace", "Italian", List.of(
                        item("pp-1", "Margherita Pizza", "12.50"),
                        item("pp-2", "Pepperoni Pizza", "14.00"),
                        item("pp-3", "Garlic Bread", "5.25"),
                        item("pp-4", "Caesar Salad", "8.75"))),
                new Restaurant("sushi-corner", "Sushi Corner", "Japanese", List.of(
                        item("sc-1", "California Roll", "9.00"),
                        item("sc-2", "Salmon Nigiri (2 pcs)", "6.50"),
                        item("sc-3", "Miso Soup", "3.25"))),
                new Restaurant("burger-barn", "Burger Barn", "American", List.of(
                        item("bb-1", "Classic Burger", "11.00"),
                        item("bb-2", "Veggie Burger", "10.50"),
                        item("bb-3", "Fries", "4.00"),
                        item("bb-4", "Milkshake", "5.50"))));
    }

    private static MenuItem item(String id, String name, String price) {
        return new MenuItem(id, name, new BigDecimal(price));
    }
}
