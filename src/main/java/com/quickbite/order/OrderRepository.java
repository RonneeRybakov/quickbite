
package com.quickbite.order;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Stores submitted orders in memory. */
public class OrderRepository {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    /** Saves an order. */
    public void save(Order order) {
        if (orders.putIfAbsent(order.id(), order) != null) {
            throw new IllegalArgumentException("Order ID already exists.");
        }
    }

    /** Finds an order by its unique ID. */
    public Optional<Order> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(orders.get(id));
    }

    /** Returns all saved orders. */
    public Collection<Order> findAll() {
        return java.util.List.copyOf(orders.values());
    }

    /** Returns the number of saved orders. */
    public int count() {
        return orders.size();
    }
}
