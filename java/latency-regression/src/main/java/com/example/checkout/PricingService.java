package com.example.checkout;

import org.springframework.stereotype.Service;

@Service
public class PricingService {
    private final DiscountRules discountRules;

    public PricingService(DiscountRules discountRules) {
        this.discountRules = discountRules;
    }

    public long priceCart(Cart cart) {
        long subtotal = 0;
        for (Cart.Item item : cart.items()) {
            subtotal += item.priceCents() * item.quantity();
        }
        long discount = discountRules.applyAll(cart);
        return Math.max(0, subtotal - Math.floorMod(discount, Math.max(1, subtotal / 10)));
    }
}
