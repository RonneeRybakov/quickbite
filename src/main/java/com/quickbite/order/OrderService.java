
package com.quickbite.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.quickbite.cart.Cart;
import com.quickbite.cart.CartLine;
import com.quickbite.pickup.PickupSlots;

/** Handles order validation and submission. */
public class OrderService {

    private final OrderRepository repository;
    private final PickupSlots pickupSlots;

    public OrderService(OrderRepository repository, PickupSlots pickupSlots) {
        this.repository = repository;
        this.pickupSlots = pickupSlots;
    }

    /**
     * Validates and submits an order.
     * The cart is cleared only after the order is saved.
     *
     * @throws IllegalArgumentException if the order is invalid
     */
    public Order submit(Cart cart, String pickupTimeInput) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot submit an empty cart.");
        }

        if (cart.getRestaurant() == null) {
            throw new IllegalArgumentException(
                    "The cart has no restaurant.");
        }

        List<CartLine> items = new ArrayList<>(cart.getLines());

        for (CartLine line : items) {
            if (line == null || line.item() == null
                    || line.quantity() <= 0
                    || line.item().price() == null
                    || line.item().price().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "The cart contains an invalid item or quantity.");
            }

            boolean belongsToRestaurant = cart.getRestaurant().menu()
                    .stream()
                    .anyMatch(item -> item.id().equals(line.item().id())
                            && item.equals(line.item()));

            if (!belongsToRestaurant) {
                throw new IllegalArgumentException(
                        "The cart contains an item from another restaurant.");
            }
        }

        if (!pickupSlots.isValid(cart.getRestaurant(), pickupTimeInput)) {
            throw new IllegalArgumentException(
                    "Please select a valid future pickup time.");
        }

        LocalDateTime pickupTime = LocalDateTime.parse(pickupTimeInput);

        BigDecimal total = items.stream()
                .map(CartLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String generatedId = "QB-" + (1000 + repository.count() + 1);

        Order order = new Order(
                generatedId,
                cart.getRestaurant(),
                items,
                total,
                pickupTime,
                Order.Status.RECEIVED);

        repository.save(order);

        cart.clear();

        return order;
    }

    /** Finds a previously submitted order. */
    public Order findById(String id) {
        return repository.findById(id).orElse(null);
    }
}
