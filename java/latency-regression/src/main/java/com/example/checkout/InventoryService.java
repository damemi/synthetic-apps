package com.example.checkout;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final int work = Work.intEnv("INVENTORY_WORK", 1_500_000);

    public long reserve(Cart cart) {
        long x = cart.items().size() | 1;
        for (int i = 0; i < work; i++) {
            x ^= x << 13;
            x ^= x >>> 7;
            x ^= x << 17;
        }
        return x;
    }
}
