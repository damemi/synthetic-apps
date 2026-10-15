# java-latency-regression

A Spring Boot checkout service with an on-CPU latency regression you can switch on at runtime. It exists to exercise
the Insights **D3 latency** trigger and the interrogation investigation loop: learn a baseline, flip the toggle, and
check that the investigation zooms in on `DiscountRules.evaluateRule` without being told where to look.

No `InstrumentationRule` ships with the app on purpose. The interrogation loop is supposed to choose its own probes.

## Endpoints

| Endpoint | Purpose |
| --- | --- |
| `GET /checkout?items=N` | Places an order for a deterministic cart of `N` items (default 5, max 2000). |
| `POST /admin/regression?enabled=true\|false` | Turns the regression on or off. In-process state, so a restart resets it. |
| `GET /admin/regression` | Shows the current toggle. |
| `GET /health` | Liveness and readiness. |

`REGRESSION_ENABLED` (default `false`) sets the toggle at startup.

## Call chain

```
CheckoutController.checkout
  CheckoutService.placeOrder
    InventoryService.reserve        fixed CPU work
    PricingService.priceCart
      DiscountRules.applyAll        8 rules
        DiscountRules.evaluateRule  regression: per item when off, every item pair when on
    PaymentService.authorize        fixed CPU work
```

All work is busy-looping on the CPU (no sleeps), so CPU profiles see the time in the method that spends it. Each loop is
written out in its own method rather than shared, so the regressed method is also the deepest application frame. The
JVM runs with `-XX:CompileCommand=dontinline,com.example.checkout.*::*` so the JIT keeps every one of these methods as
its own frame.

With the toggle off, a request costs about the same at any cart size (single-digit to low-teens milliseconds). With it
on, cost grows with the square of the cart: small carts barely change, and carts of 150-400 items gain roughly
50-400 ms. Tune with `INVENTORY_WORK`, `PAYMENT_WORK`, `ITEM_WORK` and `PAIR_WORK` (loop iterations) if your machine is
much faster or slower.

### Probe targets

Java custom probes are `className` plus `methodName`:

| className | methodName |
| --- | --- |
| `com.example.checkout.CheckoutController` | `checkout` |
| `com.example.checkout.CheckoutService` | `placeOrder` |
| `com.example.checkout.InventoryService` | `reserve` |
| `com.example.checkout.PricingService` | `priceCart` |
| `com.example.checkout.DiscountRules` | `applyAll` |
| `com.example.checkout.DiscountRules` | `evaluateRule` |
| `com.example.checkout.PaymentService` | `authorize` |

## Build and deploy

Prerequisites: a kind cluster with Odigos and Insights installed, with Java profiling and interrogation enabled.

```bash
cd java/latency-regression
make deploy     # docker build, kind load, apply Deployment, Service and Source, restart
make loadgen    # steady traffic: about 10 rps, 70% carts of 1-20 items, 30% of 150-400
```

## Test flow

Insights must run with interrogation enabled and `INTERROGATION_AUTO_APPLY=true`; without auto-apply the investigation
intentionally stops after recording the proposed `DiscountRules.evaluateRule` candidate.

1. Deploy the app and start baseline traffic with the regression off:
   ```bash
   make deploy
   make loadgen
   make regression-status
   ```
2. Wait for the `GET /checkout` transaction to promote its baselines (check the Insights transactions page). The
   default learning policy needs a few hundred traces, so a few minutes at 10 rps.
3. Enable the regression. About 30% of requests (the large carts) now land above the learned p99:
   ```bash
   make regression-on
   ```
4. Expect a sustained D3 deviation rate on `GET /checkout` in `deviation_windows`. D3 alone stays under the default
   `fire_at_score`, so it does not show up as an anomaly issue. Open `/debug/investigations`; after the trigger's
   ten-minute lookback contains at least 20 deviated traces across two minute buckets, an investigation appears in
   `collecting`.
5. With no probes yet, the trace has only the HTTP server span, so all the extra time sits under it. The first
   investigation round should follow the profile through `CheckoutService.placeOrder`, `PricingService.priceCart` and
   `DiscountRules.applyAll` and nominate `DiscountRules.evaluateRule` as the method to probe. Confirm the owned rule:
   ```bash
   kubectl get instrumentationrules -A
   kubectl get instrumentationrules -A -o yaml | grep -A8 -B4 evaluateRule
   ```
6. Open the investigation detail page. Round 0 shows the slow/fast cohort counts, entry delta, heavy-path analysis,
   candidate, and live rule. Once round 1 has 10 traces in each cohort, the `evaluateRule` custom span should explain
   at least 60% of the entry delta; the page moves to `concluded / done`, the probe ledger says `kept`, and the final
   explanation percentage is shown.
7. Recover and clean up:
   ```bash
   make regression-off
   make loadgen-stop
   # Or remove the whole demo namespace:
   make clean
   ```
