package com.quickbite.menu;

import java.util.List;
import java.util.Optional;

public record Restaurant(String id, String name, String cuisine, List<MenuItem> menu) {

    public Optional<MenuItem> findItem(String itemId) {
        return menu.stream().filter(item -> item.id().equals(itemId)).findFirst();
    }
}
