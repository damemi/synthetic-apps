package com.example.checkout;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

/** Runtime switch for the DiscountRules regression; starts from REGRESSION_ENABLED. */
@Component
public class RegressionToggle {
    private final AtomicBoolean enabled = new AtomicBoolean(Boolean.parseBoolean(System.getenv("REGRESSION_ENABLED")));

    public boolean enabled() {
        return enabled.get();
    }

    public void set(boolean on) {
        enabled.set(on);
    }
}
