
package com.quickbite;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.quickbite.cart.Cart;
import com.quickbite.menu.MenuItem;
import com.quickbite.menu.Restaurant;
import com.quickbite.menu.RestaurantCatalog;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/** Starts the QuickBite web app using the HTTP server built into the JDK. */
public class App {

    private static final int PORT = 8080;

    private final RestaurantCatalog catalog = new RestaurantCatalog();
    private final Map<String, Visitor> visitors = new ConcurrentHashMap<>();

    /** What we remember about one browser between requests. */
    private static class Visitor {
        final Cart cart = new Cart();
        String message;
        String error;
    }

    public static void main(String[] args) throws IOException {
        App app = new App();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", app::handle);
        server.start();
        System.out.println("QuickBite running at http://localhost:" + PORT);
    }

    private void handle(HttpExchange exchange) throws IOException {
        Visitor visitor = visitorFor(exchange);

        synchronized (visitor) {
            handleRequest(exchange, visitor);
        }
    }

    private void handleRequest(HttpExchange exchange, Visitor visitor) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/")) {
            // PBI-01: browse restaurants
            sendHtml(exchange, 200, Pages.restaurants(catalog.findAll(), visitor.cart));

        } else if (method.equals("GET") && path.startsWith("/restaurants/")) {
            // PBI-02: view a restaurant's menu
            Optional<Restaurant> restaurant = catalog.findById(path.substring("/restaurants/".length()));
            if (restaurant.isEmpty()) {
                sendHtml(exchange, 404, Pages.notFound(visitor.cart));
                return;
            }
            sendHtml(exchange, 200, Pages.menu(restaurant.get(), visitor.cart, takeMessage(visitor), takeError(visitor)));

        } else if (method.equals("GET") && path.equals("/cart")) {
            // PBI-03 and PBI-04: view and edit the cart
            sendHtml(exchange, 200, Pages.cart(visitor.cart, takeMessage(visitor), takeError(visitor)));

        } else if (method.equals("POST") && path.equals("/cart/add")) {
            addToCart(exchange, visitor);

        } else if (method.equals("POST") && path.equals("/cart/update")) {
            // PBI-04: update item quantity
            updateCart(exchange, visitor);

        } else if (method.equals("POST") && path.equals("/cart/remove")) {
            // PBI-04: remove an individual item
            removeFromCart(exchange, visitor);

        } else if (method.equals("POST") && path.equals("/cart/clear")) {
            visitor.cart.clear();
            visitor.message = "Your cart has been cleared.";
            redirect(exchange, "/cart");

        } else {
            sendHtml(exchange, 404, Pages.notFound(visitor.cart));
        }
    }

    private void addToCart(HttpExchange exchange, Visitor visitor) throws IOException {
        Map<String, String> form = readForm(exchange);
        Optional<Restaurant> restaurant = catalog.findById(form.getOrDefault("restaurantId", ""));
        Optional<MenuItem> item = restaurant.flatMap(r -> r.findItem(form.getOrDefault("itemId", "")));
        if (item.isEmpty()) {
            sendHtml(exchange, 404, Pages.notFound(visitor.cart));
            return;
        }

        if (visitor.cart.add(restaurant.get(), item.get())) {
            visitor.message = item.get().name() + " added to your cart.";
            visitor.error = null;
        } else {
            visitor.error = "Your cart has items from " + visitor.cart.getRestaurant().name()
                    + ". Clear your cart before ordering from another restaurant.";
            visitor.message = null;
        }
        redirect(exchange, "/restaurants/" + restaurant.get().id());
    }

    /** PBI-04: Updates the quantity of an item in the cart. */
    private void updateCart(HttpExchange exchange, Visitor visitor) throws IOException {
        Map<String, String> form = readForm(exchange);
        String itemId = form.getOrDefault("itemId", "");
        String quantityInput = form.getOrDefault("quantity", "");

        visitor.message = null;
        visitor.error = null;

        try {
            int quantity = Integer.parseInt(quantityInput);

            if (visitor.cart.updateQuantity(itemId, quantity)) {
                visitor.message = "Item quantity updated.";
            } else {
                visitor.error = "Invalid quantity or item.";
            }

        } catch (NumberFormatException e) {
            visitor.error = "Please enter a positive whole number.";
        }

        redirect(exchange, "/cart");
    }

    /** PBI-04: Removes an individual item from the cart. */
    private void removeFromCart(HttpExchange exchange, Visitor visitor) throws IOException {
        Map<String, String> form = readForm(exchange);
        String itemId = form.getOrDefault("itemId", "");

        visitor.message = null;
        visitor.error = null;

        if (visitor.cart.removeItem(itemId)) {
            visitor.message = "Item removed from your cart.";
        } else {
            visitor.error = "Item not found in your cart.";
        }

        redirect(exchange, "/cart");
    }

    // --- Small HTTP helpers ---

    /** Finds the visitor by their session cookie, or starts a new session. */
    private Visitor visitorFor(HttpExchange exchange) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String cookie : cookies.split(";")) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair[0].equals("session") && pair.length == 2 && visitors.containsKey(pair[1])) {
                    return visitors.get(pair[1]);
                }
            }
        }
        String id = UUID.randomUUID().toString();
        Visitor visitor = new Visitor();
        visitors.put(id, visitor);
        exchange.getResponseHeaders().add("Set-Cookie", "session=" + id + "; Path=/; HttpOnly; SameSite=Lax");
        return visitor;
    }

    private static String takeMessage(Visitor visitor) {
        String message = visitor.message;
        visitor.message = null;
        return message;
    }

    private static String takeError(Visitor visitor) {
        String error = visitor.error;
        visitor.error = null;
        return error;
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();
        for (String field : body.split("&")) {
            String[] pair = field.split("=", 2);
            if (pair.length == 2) {
                form.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return form;
    }

    private static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    /** Sends the browser to another page after a form post (Post/Redirect/Get). */
    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }
}
