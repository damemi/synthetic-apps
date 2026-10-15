package com.example.checkout;

import org.springframework.stereotype.Service;

@Service
public class CheckoutService {
    public record Order(long totalCents, long reservation, long authorization) {
    }

    private final InventoryService inventory;
    private final PricingService pricing;
    private final PaymentService payment;

    public CheckoutService(InventoryService inventory, PricingService pricing, PaymentService payment) {
        this.inventory = inventory;
        this.pricing = pricing;
        this.payment = payment;
    }

    public Order placeOrder(Cart cart) {
        long reservation = inventory.reserve(cart);
        long total = pricing.priceCart(cart);
        long authorization = payment.authorize(total);
        return new Order(total, reservation, authorization);
    }
}
