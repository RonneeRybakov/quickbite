
package com.quickbite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.quickbite.cart.Cart;
import com.quickbite.cart.CartLine;
import com.quickbite.menu.MenuItem;
import com.quickbite.menu.Restaurant;
import com.quickbite.order.Order;

/** Builds the HTML for each page. */
public final class Pages {

    private Pages() {
    }

    private static final DateTimeFormatter PICKUP_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    private static final String STYLE = """
            body { font-family: Arial, sans-serif; margin: 0; color: #222; }
            header { display: flex; justify-content: space-between; padding: 12px 24px; background: #d9480f; }
            header a { color: white; text-decoration: none; margin-left: 16px; }
            header .brand { font-weight: bold; font-size: 1.3em; margin-left: 0; }
            main { max-width: 720px; margin: 24px auto; padding: 0 16px; }
            table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
            th, td { text-align: left; padding: 8px; border-bottom: 1px solid #ddd; }
            ul { list-style: none; padding: 0; }
            li { padding: 12px 0; border-bottom: 1px solid #ddd; }
            .muted { color: #777; }
            .message { background: #e6f4ea; padding: 8px 12px; }
            .error { background: #fdecea; padding: 8px 12px; }
            button { cursor: pointer; padding: 5px 10px; }
            input[type=number] { padding: 5px; margin-right: 6px; }
            select { padding: 8px; margin: 8px 0 16px; min-width: 250px; }
            .checkout { margin-top: 20px; }
            .summary { background: #f5f5f5; padding: 16px; margin: 20px 0; }
            """;

    /** PBI-01: list of restaurants. */
    public static String restaurants(List<Restaurant> restaurants, Cart cart) {
        StringBuilder body = new StringBuilder("<h1>Restaurants</h1>");

        if (restaurants.isEmpty()) {
            body.append("<p class='muted'>No restaurants are available right now. Please check back later.</p>");
        } else {
            body.append("<ul>");

            for (Restaurant r : restaurants) {
                body.append("<li><a href='/restaurants/")
                        .append(esc(r.id())).append("'>")
                        .append(esc(r.name()))
                        .append("</a> <span class='muted'>")
                        .append(esc(r.cuisine()))
                        .append("</span></li>");
            }

            body.append("</ul>");
        }

        return layout("Restaurants", cart, body.toString());
    }

    /** PBI-02: restaurant menu with Add to cart buttons. */
    public static String menu(Restaurant restaurant, Cart cart,
                              String message, String error) {

        StringBuilder body = new StringBuilder(
                "<p><a href='/'>&larr; All restaurants</a></p>")
                .append("<h1>").append(esc(restaurant.name())).append("</h1>")
                .append(flash(message, error));

        if (restaurant.menu().isEmpty()) {
            body.append("<p class='muted'>This restaurant has no menu items available right now.</p>");
        } else {
            body.append("<table><tr><th>Item</th><th>Price</th><th></th></tr>");

            for (MenuItem item : restaurant.menu()) {
                body.append("<tr><td>").append(esc(item.name()))
                        .append("</td><td>").append(money(item.price()))
                        .append("</td><td><form method='post' action='/cart/add'>")
                        .append("<input type='hidden' name='restaurantId' value='")
                        .append(esc(restaurant.id())).append("'>")
                        .append("<input type='hidden' name='itemId' value='")
                        .append(esc(item.id())).append("'>")
                        .append("<button>Add to cart</button></form></td></tr>");
            }

            body.append("</table>");
        }

        return layout(restaurant.name(), cart, body.toString());
    }

    /** PBI-03 and PBI-04: view and edit the cart. */
    public static String cart(Cart cart, String message, String error) {
        StringBuilder body = new StringBuilder("<h1>Your cart</h1>")
                .append(flash(message, error));

        if (cart.isEmpty()) {
            body.append("<p class='muted'>Your cart is empty. ")
                    .append("Add items from a restaurant before placing an order. ")
                    .append("<a href='/'>Browse restaurants</a></p>");
        } else {
            Restaurant r = cart.getRestaurant();

            body.append("<p>Ordering from <a href='/restaurants/")
                    .append(esc(r.id())).append("'>")
                    .append(esc(r.name())).append("</a></p>")
                    .append("<table><tr><th>Item</th><th>Quantity</th>")
                    .append("<th>Subtotal</th><th>Action</th></tr>");

            for (CartLine line : cart.getLines()) {
                String itemId = esc(line.item().id());

                body.append("<tr><td>")
                        .append(esc(line.item().name()))
                        .append("</td><td>")
                        .append("<form method='post' action='/cart/update'>")
                        .append("<input type='hidden' name='itemId' value='")
                        .append(itemId).append("'>")
                        .append("<input type='number' name='quantity' min='1' step='1' value='")
                        .append(line.quantity())
                        .append("' required style='width:60px'>")
                        .append("<button type='submit'>Update</button>")
                        .append("</form></td><td>")
                        .append(money(line.subtotal()))
                        .append("</td><td>")
                        .append("<form method='post' action='/cart/remove'>")
                        .append("<input type='hidden' name='itemId' value='")
                        .append(itemId).append("'>")
                        .append("<button type='submit'>Remove</button>")
                        .append("</form></td></tr>");
            }

            body.append("<tr><th colspan='2'>Total</th><th>")
                    .append(money(cart.getTotal()))
                    .append("</th><th></th></tr></table>")
                    .append("<form method='post' action='/cart/clear'>")
                    .append("<button type='submit'>Clear cart</button>")
                    .append("</form>")
                    .append("<div class='checkout'>")
                    .append("<a href='/checkout'>Proceed to Checkout</a>")
                    .append("</div>");
        }

        return layout("Your cart", cart, body.toString());
    }

    /** PBI-05: Displays pickup times during checkout. */
    public static String checkout(Cart cart, List<LocalDateTime> slots,
                                  String selected, String error) {

        StringBuilder body = new StringBuilder("<h1>Checkout</h1>")
                .append(flash(null, error));

        if (cart.isEmpty()) {
            body.append("<p>Your cart is empty. ")
                    .append("<a href='/'>Browse restaurants</a></p>");
            return layout("Checkout", cart, body.toString());
        }

        Restaurant restaurant = cart.getRestaurant();

        body.append("<p>Restaurant: <strong>")
                .append(esc(restaurant.name()))
                .append("</strong></p>")
                .append("<h2>Order details</h2>")
                .append(orderItems(cart.getLines(), cart.getTotal()))
                .append("<h2>Select a pickup time</h2>");

        if (slots.isEmpty()) {
            body.append("<p class='error'>No pickup times are currently available.</p>");
        } else {
            body.append("<form method='post' action='/checkout'>")
                    .append("<label for='pickupTime'>Available pickup times:</label>")
                    .append("<div><select id='pickupTime' name='pickupTime' required>")
                    .append("<option value=''>-- Select a pickup time --</option>");

            for (LocalDateTime slot : slots) {
                String value = slot.toString();

                body.append("<option value='")
                        .append(esc(value)).append("'");

                if (value.equals(selected)) {
                    body.append(" selected");
                }

                body.append(">")
                        .append(esc(slot.format(PICKUP_FORMAT)))
                        .append("</option>");
            }

            body.append("</select></div>")
                    .append("<button type='submit'>Submit Order</button>")
                    .append("</form>");
        }

        body.append("<p><a href='/cart'>&larr; Back to cart</a></p>");

        return layout("Checkout", cart, body.toString());
    }

    /** PBI-06: Displays a saved order and its Received status. */
    public static String orderSummary(Order order, Cart cart) {
        StringBuilder body = new StringBuilder("<h1>Order submitted!</h1>")
                .append("<p class='message'>Your order has been received.</p>")
                .append("<div class='summary'>")
                .append("<h2>Order summary</h2>")
                .append("<p><strong>Order ID:</strong> ")
                .append(esc(order.id()))
                .append("</p>")
                .append("<p><strong>Status:</strong> ")
                .append(esc(order.status().name()))
                .append("</p>")
                .append("<p><strong>Restaurant:</strong> ")
                .append(esc(order.restaurant().name()))
                .append("</p>")
                .append("<p><strong>Pickup time:</strong> ")
                .append(esc(order.pickupTime().format(PICKUP_FORMAT)))
                .append("</p>")
                .append(orderItems(order.items(), order.total()))
                .append("</div>")
                .append("<p><a href='/'>Back to restaurants</a></p>");

        return layout("Order Summary", cart, body.toString());
    }

    /** Displays order items, quantities, subtotals and total. */
    private static String orderItems(
            java.util.Collection<CartLine> items, BigDecimal total) {

        StringBuilder body = new StringBuilder(
                "<table><tr><th>Item</th><th>Quantity</th><th>Subtotal</th></tr>");

        for (CartLine line : items) {
            body.append("<tr><td>")
                    .append(esc(line.item().name()))
                    .append("</td><td>")
                    .append(line.quantity())
                    .append("</td><td>")
                    .append(money(line.subtotal()))
                    .append("</td></tr>");
        }

        body.append("<tr><th colspan='2'>Total</th><th>")
                .append(money(total))
                .append("</th></tr></table>");

        return body.toString();
    }

    public static String notFound(Cart cart) {
        return layout("Not found", cart,
                "<h1>Page not found</h1><p><a href='/'>Back to restaurants</a></p>");
    }

    private static String layout(String title, Cart cart, String body) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><title>QuickBite - "
                + esc(title) + "</title>"
                + "<style>" + STYLE + "</style></head><body>"
                + "<header><a class='brand' href='/'>QuickBite</a>"
                + "<nav><a href='/'>Restaurants</a>"
                + "<a href='/cart'>Cart (" + cart.getItemCount()
                + ")</a></nav></header>"
                + "<main>" + body + "</main></body></html>";
    }

    private static String flash(String message, String error) {
        return (message != null
                ? "<p class='message'>" + esc(message) + "</p>" : "")
                + (error != null
                ? "<p class='error'>" + esc(error) + "</p>" : "");
    }

    private static String money(BigDecimal amount) {
        return "$" + amount.setScale(2, RoundingMode.HALF_UP);
    }

    /** Escapes text so it is shown as-is in HTML. */
    private static String esc(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;");
    }
}
