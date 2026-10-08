
package com.quickbite.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.quickbite.cart.CartLine;
import com.quickbite.menu.Restaurant;

/** A submitted customer order. */
public record Order(
        String id,
        Restaurant restaurant,
        List<CartLine> items,
        BigDecimal total,
        LocalDateTime pickupTime,
        Status status) {

    public enum Status {
        RECEIVED
    }

    public Order {
        items = List.copyOf(items);
    }
}
