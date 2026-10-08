package com.quickbite.menu;

import java.math.BigDecimal;

/** A dish on a restaurant's menu. The price is the final price (no extra charges). */
public record MenuItem(String id, String name, BigDecimal price) {
}
