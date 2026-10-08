
package com.quickbite.cart;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.quickbite.menu.MenuItem;
import com.quickbite.menu.Restaurant;

/** A customer's cart. It only holds items from a single restaurant. */
public class Cart {

    private Restaurant restaurant;
    private final Map<String, CartLine> lines = new LinkedHashMap<>();

    /**
     * Adds one unit of the item. Adding the same item again increases its quantity.
     *
     * @return false if the cart already holds items from a different restaurant
     */
    public boolean add(Restaurant from, MenuItem item) {
        if (!isEmpty() && !restaurant.id().equals(from.id())) {
            return false;
        }
        restaurant = from;
        lines.merge(item.id(), new CartLine(item, 1),
                (existing, added) -> new CartLine(existing.item(), existing.quantity() + 1));
        return true;
    }

    /**
     * Updates the quantity of an item already in the cart.
     * Only positive whole-number quantities are accepted.
     *
     * @return false if the quantity is invalid or the item does not exist
     */
    public boolean updateQuantity(String itemId, int quantity) {
        if (quantity <= 0 || !lines.containsKey(itemId)) {
            return false;
        }

        CartLine existing = lines.get(itemId);
        lines.put(itemId, new CartLine(existing.item(), quantity));
        return true;
    }

    /**
     * Removes an individual item from the cart.
     *
     * @return false if the item does not exist
     */
    public boolean removeItem(String itemId) {
        if (lines.remove(itemId) == null) {
            return false;
        }

        if (lines.isEmpty()) {
            restaurant = null;
        }

        return true;
    }

    public void clear() {
        lines.clear();
        restaurant = null;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /** The restaurant the cart's items come from, or null when the cart is empty. */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    public Collection<CartLine> getLines() {
        return java.util.List.copyOf(lines.values());
    }

    public int getItemCount() {
        return lines.values().stream().mapToInt(CartLine::quantity).sum();
    }

    public BigDecimal getTotal() {
        return lines.values().stream().map(CartLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
