package com.example.checkout;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * The regression lives here. With the toggle off each rule looks at every item
 * once; with it on, evaluateRule compares every item against every other item,
 * so cost grows with the square of the cart size and small carts barely change.
 */
@Component
public class DiscountRules {
    private static final int RULES = 8;

    private final RegressionToggle regression;
    private final int itemWork = Work.intEnv("ITEM_WORK", 2_000);
    private final int pairWork = Work.intEnv("PAIR_WORK", 200);

    public DiscountRules(RegressionToggle regression) {
        this.regression = regression;
    }

    public long applyAll(Cart cart) {
        boolean pairwise = regression.enabled();
        long acc = 0;
        for (int rule = 0; rule < RULES; rule++) {
            acc += evaluateRule(rule, cart.items(), pairwise);
        }
        return acc;
    }

    public long evaluateRule(int rule, List<Cart.Item> items, boolean pairwise) {
        long acc = rule;
        int n = items.size();
        for (int i = 0; i < n; i++) {
            Cart.Item a = items.get(i);
            if (!pairwise) {
                long x = (a.priceCents() ^ rule) | 1;
                for (int k = 0; k < itemWork; k++) {
                    x ^= x << 13;
                    x ^= x >>> 7;
                    x ^= x << 17;
                }
                acc += x;
                continue;
            }
            for (int j = 0; j < n; j++) {
                Cart.Item b = items.get(j);
                long x = ((a.priceCents() * 31 + b.priceCents()) ^ a.sku().hashCode() ^ rule) | 1;
                for (int k = 0; k < pairWork; k++) {
                    x ^= x << 13;
                    x ^= x >>> 7;
                    x ^= x << 17;
                }
                acc += x;
            }
        }
        return acc;
    }
}
