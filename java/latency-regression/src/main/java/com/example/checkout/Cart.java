package com.example.checkout;

import java.util.ArrayList;
import java.util.List;

public final class Cart {
    public record Item(String sku, int quantity, long priceCents) {
    }

    private final List<Item> items;

    private Cart(List<Item> items) {
        this.items = items;
    }

    public List<Item> items() {
        return items;
    }

    /** Deterministic cart of n items, so the same size always costs the same. */
    public static Cart ofSize(int n) {
        List<Item> items = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            items.add(new Item("sku-" + (i % 97), 1 + (i % 3), 199 + (i * 37L) % 4800));
        }
        return new Cart(items);
    }
}
