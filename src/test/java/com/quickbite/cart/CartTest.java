
package com.quickbite.cart;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.quickbite.menu.MenuItem;
import com.quickbite.menu.Restaurant;

class CartTest {

    private final MenuItem pizza = new MenuItem("p1", "Pizza", new BigDecimal("12.50"));
    private final MenuItem bread = new MenuItem("p2", "Garlic Bread", new BigDecimal("5.25"));
    private final MenuItem roll = new MenuItem("s1", "California Roll", new BigDecimal("9.00"));
    private final Restaurant pizzaPlace = new Restaurant("pizza", "Pizza Place", "Italian", List.of(pizza, bread));
    private final Restaurant sushiPlace = new Restaurant("sushi", "Sushi Place", "Japanese", List.of(roll));

    private final Cart cart = new Cart();

    @Test
    void addingTheSameItemAgainIncreasesItsQuantity() {
        cart.add(pizzaPlace, pizza);
        cart.add(pizzaPlace, pizza);

        assertEquals(1, cart.getLines().size());
        assertEquals(2, cart.getLines().iterator().next().quantity());
    }

    @Test
    void subtotalsAndTotalAreCalculated() {
        cart.add(pizzaPlace, pizza);
        cart.add(pizzaPlace, pizza);
        cart.add(pizzaPlace, bread);

        assertEquals(new BigDecimal("25.00"), cart.getLines().iterator().next().subtotal());
        assertEquals(new BigDecimal("30.25"), cart.getTotal());
    }

    @Test
    void itemsFromAnotherRestaurantAreRejectedUntilCartIsCleared() {
        cart.add(pizzaPlace, pizza);
        assertFalse(cart.add(sushiPlace, roll));

        cart.clear();
        assertTrue(cart.add(sushiPlace, roll));
    }

    // --- PBI-04: Edit the cart ---

    @Test
    void updatingQuantityChangesSubtotalAndTotal() {
        cart.add(pizzaPlace, pizza);
        cart.add(pizzaPlace, bread);

        assertTrue(cart.updateQuantity("p1", 3));

        assertEquals(3, cart.getLines().iterator().next().quantity());
        assertEquals(new BigDecimal("37.50"), cart.getLines().iterator().next().subtotal());
        assertEquals(new BigDecimal("42.75"), cart.getTotal());
    }

    @Test
    void updatingQuantityDoesNotChangeOtherItems() {
        cart.add(pizzaPlace, pizza);
        cart.add(pizzaPlace, bread);

        assertTrue(cart.updateQuantity("p1", 2));

        assertEquals(3, cart.getItemCount());
        assertEquals(new BigDecimal("30.25"), cart.getTotal());
    }

    @Test
    void removingAnItemUpdatesTheTotal() {
        cart.add(pizzaPlace, pizza);
        cart.add(pizzaPlace, bread);

        assertTrue(cart.removeItem("p1"));

        assertEquals(1, cart.getItemCount());
        assertEquals(new BigDecimal("5.25"), cart.getTotal());
        assertFalse(cart.isEmpty());
    }

    @Test
    void invalidQuantitiesAreRejected() {
        cart.add(pizzaPlace, pizza);

        assertFalse(cart.updateQuantity("p1", 0));
        assertFalse(cart.updateQuantity("p1", -2));

        assertEquals(1, cart.getItemCount());
        assertEquals(new BigDecimal("12.50"), cart.getTotal());
    }

    @Test
    void removingLastItemMakesCartEmpty() {
        cart.add(pizzaPlace, pizza);

        assertTrue(cart.removeItem("p1"));

        assertTrue(cart.isEmpty());
        assertNull(cart.getRestaurant());
        assertEquals(BigDecimal.ZERO, cart.getTotal());
    }

    @Test
    void cannotUpdateOrRemoveNonexistentItem() {
        cart.add(pizzaPlace, pizza);

        assertFalse(cart.updateQuantity("missing", 2));
        assertFalse(cart.removeItem("missing"));

        assertEquals(1, cart.getItemCount());
    }

    @Test
    void validPositiveQuantitiesAreAccepted() {
        cart.add(pizzaPlace, pizza);

        assertTrue(cart.updateQuantity("p1", 5));

        assertEquals(5, cart.getItemCount());
        assertEquals(new BigDecimal("62.50"), cart.getTotal());
    }

    @Test
    void removingItemAllowsOrderingFromAnotherRestaurant() {
        cart.add(pizzaPlace, pizza);
        cart.removeItem("p1");

        assertTrue(cart.add(sushiPlace, roll));
        assertEquals(sushiPlace, cart.getRestaurant());
    }

    @Test
    void cartLinesCannotBeModifiedDirectly() {
        cart.add(pizzaPlace, pizza);

        assertThrows(UnsupportedOperationException.class,
                () -> cart.getLines().clear());

        assertEquals(1, cart.getItemCount());
    }
}
