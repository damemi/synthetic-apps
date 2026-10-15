package com.example.checkout;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final int work = Work.intEnv("PAYMENT_WORK", 1_500_000);

    public long authorize(long totalCents) {
        long x = totalCents | 1;
        for (int i = 0; i < work; i++) {
            x ^= x << 13;
            x ^= x >>> 7;
            x ^= x << 17;
        }
        return x;
    }
}
