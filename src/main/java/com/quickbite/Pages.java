package com.quickbite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.quickbite.cart.Cart;
import com.quickbite.cart.CartLine;
import com.quickbite.menu.MenuItem;
import com.quickbite.menu.Restaurant;

/** Builds the HTML for each page. */
public final class Pages {

    private Pages() {
    }

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
            """;

    /** PBI-01: list of restaurants. */
    public static String restaurants(List<Restaurant> restaurants, Cart cart) {
        StringBuilder body = new StringBuilder("<h1>Restaurants</h1>");
        if (restaurants.isEmpty()) {
            body.append("<p class='muted'>No restaurants are available right now. Please check back later.</p>");
        } else {
            body.append("<ul>");
            for (Restaurant r : restaurants) {
                body.append("<li><a href='/restaurants/").append(esc(r.id())).append("'>").append(esc(r.name()))
                        .append("</a> <span class='muted'>").append(esc(r.cuisine())).append("</span></li>");
            }
            body.append("</ul>");
        }
        return layout("Restaurants", cart, body.toString());
    }

    /** PBI-02: a restaurant's menu, with an "Add to cart" button per item (PBI-03). */
    public static String menu(Restaurant restaurant, Cart cart, String message, String error) {
        StringBuilder body = new StringBuilder("<p><a href='/'>&larr; All restaurants</a></p>")
                .append("<h1>").append(esc(restaurant.name())).append("</h1>")
                .append(flash(message, error));
        if (restaurant.menu().isEmpty()) {
            body.append("<p class='muted'>This restaurant has no menu items available right now.</p>");
        } else {
            body.append("<table><tr><th>Item</th><th>Price</th><th></th></tr>");
            for (MenuItem item : restaurant.menu()) {
                body.append("<tr><td>").append(esc(item.name())).append("</td><td>").append(money(item.price()))
                        .append("</td><td><form method='post' action='/cart/add'>")
                        .append("<input type='hidden' name='restaurantId' value='").append(esc(restaurant.id())).append("'>")
                        .append("<input type='hidden' name='itemId' value='").append(esc(item.id())).append("'>")
                        .append("<button>Add to cart</button></form></td></tr>");
            }
            body.append("</table>");
        }
        return layout(restaurant.name(), cart, body.toString());
    }

    /** PBI-03: the cart with quantities, subtotals and total. */
    public static String cart(Cart cart, String message) {
        StringBuilder body = new StringBuilder("<h1>Your cart</h1>").append(flash(message, null));
        if (cart.isEmpty()) {
            body.append("<p class='muted'>Your cart is empty. <a href='/'>Browse restaurants</a></p>");
        } else {
            Restaurant r = cart.getRestaurant();
            body.append("<p>Ordering from <a href='/restaurants/").append(esc(r.id())).append("'>")
                    .append(esc(r.name())).append("</a></p>")
                    .append("<table><tr><th>Item</th><th>Quantity</th><th>Subtotal</th></tr>");
            for (CartLine line : cart.getLines()) {
                body.append("<tr><td>").append(esc(line.item().name())).append("</td><td>").append(line.quantity())
                        .append("</td><td>").append(money(line.subtotal())).append("</td></tr>");
            }
            body.append("<tr><th colspan='2'>Total</th><th>").append(money(cart.getTotal())).append("</th></tr></table>")
                    .append("<form method='post' action='/cart/clear'><button>Clear cart</button></form>");
        }
        return layout("Your cart", cart, body.toString());
    }

    public static String notFound(Cart cart) {
        return layout("Not found", cart, "<h1>Page not found</h1><p><a href='/'>Back to restaurants</a></p>");
    }

    private static String layout(String title, Cart cart, String body) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><title>QuickBite - " + esc(title) + "</title>"
                + "<style>" + STYLE + "</style></head><body>"
                + "<header><a class='brand' href='/'>QuickBite</a><nav><a href='/'>Restaurants</a>"
                + "<a href='/cart'>Cart (" + cart.getItemCount() + ")</a></nav></header>"
                + "<main>" + body + "</main></body></html>";
    }

    private static String flash(String message, String error) {
        return (message != null ? "<p class='message'>" + esc(message) + "</p>" : "")
                + (error != null ? "<p class='error'>" + esc(error) + "</p>" : "");
    }

    private static String money(BigDecimal amount) {
        return "$" + amount.setScale(2, RoundingMode.HALF_UP);
    }

    /** Escapes text so it is shown as-is in HTML. */
    private static String esc(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("'", "&#39;").replace("\"", "&quot;");
    }
}
