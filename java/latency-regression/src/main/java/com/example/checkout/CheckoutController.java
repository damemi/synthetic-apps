package com.example.checkout;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckoutController {
    private static final int MAX_ITEMS = 2000;

    private final CheckoutService checkoutService;
    private final RegressionToggle regression;

    public CheckoutController(CheckoutService checkoutService, RegressionToggle regression) {
        this.checkoutService = checkoutService;
        this.regression = regression;
    }

    @GetMapping("/checkout")
    public Map<String, Object> checkout(@RequestParam(name = "items", defaultValue = "5") int items) {
        int n = Math.max(1, Math.min(items, MAX_ITEMS));
        CheckoutService.Order order = checkoutService.placeOrder(Cart.ofSize(n));
        return Map.of(
                "items", n,
                "totalCents", order.totalCents(),
                "reservation", order.reservation(),
                "authorization", order.authorization());
    }

    @PostMapping("/admin/regression")
    public Map<String, Object> setRegression(@RequestParam("enabled") boolean enabled) {
        regression.set(enabled);
        return Map.of("regression", regression.enabled());
    }

    @GetMapping("/admin/regression")
    public Map<String, Object> getRegression() {
        return Map.of("regression", regression.enabled());
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
