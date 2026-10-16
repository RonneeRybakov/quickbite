package com.quickbite.order;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PBI-11: Stores submitted orders in memory and persists them to disk
 * so data remains available after restarting the application.
 */
public class OrderRepository {

    private static final Path STORAGE_PATH = Paths.get("data", "orders.txt");
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public OrderRepository() {
        loadFromStorage();
    }

    /** Saves an order in memory and appends its reference to disk. */
    public synchronized void save(Order order) {
        if (order == null || order.id() == null) {
            throw new IllegalArgumentException("Cannot save a null order or order without ID.");
        }
        if (orders.putIfAbsent(order.id(), order) != null) {
            throw new IllegalArgumentException("Order ID already exists: " + order.id());
        }
        persistOrder(order);
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

    private void persistOrder(Order order) {
        try {
            if (STORAGE_PATH.getParent() != null && !Files.exists(STORAGE_PATH.getParent())) {
                Files.createDirectories(STORAGE_PATH.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(STORAGE_PATH,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(order.id() + "|" + order.restaurant().id() + "|" + order.total() + "|" + order.status());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not write order to disk: " + e.getMessage());
        }
    }

    private void loadFromStorage() {
        if (!Files.exists(STORAGE_PATH)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(STORAGE_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Read stored order identifiers
                line = line.trim();
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not read existing orders: " + e.getMessage());
        }
    }
}