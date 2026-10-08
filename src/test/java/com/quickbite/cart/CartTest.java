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
}
