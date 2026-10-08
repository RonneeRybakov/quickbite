
package com.quickbite.order;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.quickbite.cart.Cart;
import com.quickbite.menu.MenuItem;
import com.quickbite.menu.Restaurant;
import com.quickbite.pickup.PickupSlots;

class OrderServiceTest {

    private final MenuItem pizza = new MenuItem(
            "pp-1", "Pizza", new BigDecimal("12.50"));

    private final MenuItem bread = new MenuItem(
            "pp-2", "Garlic Bread", new BigDecimal("5.25"));

    private final Restaurant restaurant = new Restaurant(
            "pizza-palace", "Pizza Palace", "Italian",
            List.of(pizza, bread));

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-10-07T10:00:00Z"),
            ZoneOffset.UTC);

    private final OrderRepository repository = new OrderRepository();

    private final OrderService service = new OrderService(
            repository, new PickupSlots(clock));

    private static final String VALID_PICKUP = "2026-10-07T12:00";

    private Cart createCart() {
        Cart cart = new Cart();
        cart.add(restaurant, pizza);
        cart.add(restaurant, bread);
        return cart;
    }

    @Test
    void validOrderIsSaved() {
        Cart cart = createCart();

        Order order = service.submit(cart, VALID_PICKUP);

        assertNotNull(order);
        assertEquals(1, repository.count());
        assertEquals(order, service.findById(order.id()));
    }

    @Test
    void submittedOrderHasUniqueId() {
        Order first = service.submit(createCart(), VALID_PICKUP);
        Order second = service.submit(createCart(), VALID_PICKUP);

        assertNotNull(first.id());
        assertNotNull(second.id());
        assertNotEquals(first.id(), second.id());
    }

    @Test
    void submittedOrderHasReceivedStatus() {
        Order order = service.submit(createCart(), VALID_PICKUP);

        assertEquals(Order.Status.RECEIVED, order.status());
    }

    @Test
    void savedOrderContainsCorrectDetails() {
        Cart cart = createCart();
        cart.updateQuantity("pp-1", 2);

        Order order = service.submit(cart, VALID_PICKUP);

        assertEquals(restaurant, order.restaurant());
        assertEquals(2, order.items().size());
        assertEquals(2, order.items().get(0).quantity());
        assertEquals(new BigDecimal("30.25"), order.total());
        assertEquals(VALID_PICKUP, order.pickupTime().toString());
    }

    @Test
    void emptyCartIsRejected() {
        Cart cart = new Cart();

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(cart, VALID_PICKUP));

        assertEquals(0, repository.count());
    }

    @Test
    void missingPickupTimeIsRejected() {
        Cart cart = createCart();

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(cart, ""));

        assertEquals(0, repository.count());
        assertFalse(cart.isEmpty());
    }

    @Test
    void pastPickupTimeIsRejected() {
        Cart cart = createCart();

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(cart, "2026-10-06T12:00"));

        assertEquals(0, repository.count());
        assertFalse(cart.isEmpty());
    }

    @Test
    void unlistedPickupTimeIsRejected() {
        Cart cart = createCart();

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(cart, "2026-10-07T15:45"));

        assertEquals(0, repository.count());
    }

    @Test
    void invalidQuantityCannotBeSubmitted() {
        Cart cart = createCart();

        assertFalse(cart.updateQuantity("pp-1", 0));
        assertFalse(cart.updateQuantity("pp-1", -3));

        assertEquals(1, cart.getLines().iterator().next().quantity());
    }

    @Test
    void invalidCartItemIsRejected() {
        MenuItem invalid = new MenuItem(
                "bad", "Invalid", new BigDecimal("-5.00"));

        Restaurant invalidRestaurant = new Restaurant(
                "pizza-palace", "Pizza Palace", "Italian",
                List.of(invalid));

        Cart cart = new Cart();
        cart.add(invalidRestaurant, invalid);

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(cart, VALID_PICKUP));

        assertEquals(0, repository.count());
        assertFalse(cart.isEmpty());
    }

    @Test
    void successfulSubmissionClearsCart() {
        Cart cart = createCart();

        service.submit(cart, VALID_PICKUP);

        assertTrue(cart.isEmpty());
        assertNull(cart.getRestaurant());
        assertEquals(0, cart.getItemCount());
    }

    @Test
    void failedSubmissionDoesNotClearCart() {
        Cart cart = createCart();

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(cart, "invalid-time"));

        assertFalse(cart.isEmpty());
        assertEquals(2, cart.getItemCount());
    }

    @Test
    void savedOrderRemainsAfterCartIsCleared() {
        Cart cart = createCart();

        Order order = service.submit(cart, VALID_PICKUP);

        assertTrue(cart.isEmpty());
        assertEquals(2, order.items().size());
        assertEquals(new BigDecimal("17.75"), order.total());
        assertEquals(1, repository.count());
    }

    @Test
    void savedOrderItemsCannotBeModified() {
        Order order = service.submit(createCart(), VALID_PICKUP);

        assertThrows(UnsupportedOperationException.class,
                () -> order.items().clear());
    }
}
