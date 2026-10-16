package com.quickbite.cart;

import java.math.BigDecimal;

import com.quickbite.menu.MenuItem;

public record CartLine(MenuItem item, int quantity) {

    public BigDecimal subtotal() {
        return item.price().multiply(BigDecimal.valueOf(quantity));
    }
}
