package com.example.checkout;

/**
 * Work sizes, read once at startup. The busy loops themselves are written out in
 * each service method on purpose: a shared helper would be the deepest
 * application frame under every hot path and hide which method regressed.
 * Every loop folds into its return value so the JIT cannot drop it, and nothing
 * sleeps, so CPU profiles see the time in the method that spends it.
 */
final class Work {
    private Work() {
    }

    static int intEnv(String name, int fallback) {
        String v = System.getenv(name);
        if (v == null || v.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
