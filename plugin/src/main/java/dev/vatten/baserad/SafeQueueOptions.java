/*
 *    Copyright 2026 vatten <vatten.dev>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.vatten.baserad;

import dev.vatten.baserad.configs.PluginConfig;
import org.mineskin.MineSkinClient;
import org.mineskin.data.User;
import org.mineskin.options.IQueueOptions;
import org.mineskin.response.UserResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafeQueueOptions implements IQueueOptions {
    private static final Logger LOGGER = Logger.getLogger("FancyTags");

    private static final int MIN_INTERVAL_MILLIS = 100;
    private static final int MAX_INTERVAL_MILLIS = 1000;
    private static final int MIN_CONCURRENCY = 1;
    private static final int MAX_CONCURRENCY = 30;

    private static final int FAILURE_STEP_MILLIS = 500;
    private static final int MAX_PENALTY_MILLIS = 10_000;
    private static final int DECAY_MILLIS_PER_SECOND = 100;
    private static final int MAX_EFFECTIVE_INTERVAL_MILLIS = 10_000;

    private final ScheduledExecutorService scheduler;
    private final PluginConfig.MineSkin.Limits limits;

    private final AtomicInteger intervalMillis;
    private final AtomicInteger concurrency;

    private final AtomicInteger peakPenaltyMillis = new AtomicInteger(0);
    private final AtomicLong lastFailureAt = new AtomicLong(0);
    private final AtomicLong lastRefresh = new AtomicLong(0);
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private MineSkinClient client;

    public SafeQueueOptions(PluginConfig.MineSkin.Limits limits) {
        this.limits = limits;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "FancyTags-MineSkin/scheduler");
            thread.setDaemon(true);
            return thread;
        });

        if (limits.getMode() == PluginConfig.MineSkin.Limits.Mode.CUSTOM) {
            this.intervalMillis = new AtomicInteger(Math.max(limits.getIntervalMillis(), MIN_INTERVAL_MILLIS));
            this.concurrency = new AtomicInteger(Math.max(limits.getConcurrency(), MIN_CONCURRENCY));
        } else {
            // Mode.AUTO: start with safe conservative defaults (1 req/sec, concurrency 1)
            this.intervalMillis = new AtomicInteger(MAX_INTERVAL_MILLIS);
            this.concurrency = new AtomicInteger(MIN_CONCURRENCY);
        }
    }

    public void init(MineSkinClient client) {
        this.client = client;
        if (limits.getMode() == PluginConfig.MineSkin.Limits.Mode.AUTO) {
            refreshGrants();
        }
    }

    public void refreshGrantsIfNeeded() {
        if (limits.getMode() != PluginConfig.MineSkin.Limits.Mode.AUTO) return;
        long now = System.currentTimeMillis();
        if (now - lastRefresh.get() > TimeUnit.HOURS.toMillis(1)) {
            refreshGrants();
        }
    }

    private void refreshGrants() {
        if (client == null || !isRefreshing.compareAndSet(false, true)) {
            return;
        }
        lastRefresh.set(System.currentTimeMillis());

        client.misc().getUser()
                .thenApply(UserResponse::getUser)
                .thenApply(User::grants)
                .thenAccept(grants -> {
                    grants.concurrency().ifPresent(rawConcurrent -> {
                        int concurrent = Math.min(Math.max(rawConcurrent, MIN_CONCURRENCY), MAX_CONCURRENCY);
                        concurrency.set(concurrent);
                    });
                    grants.perMinute().ifPresent(rawPerMinute -> {
                        int interval = Math.min(Math.max(60_000 / rawPerMinute, MIN_INTERVAL_MILLIS), MAX_INTERVAL_MILLIS);
                        intervalMillis.set(interval);
                    });
                    LOGGER.log(Level.FINE, "[FancyTags] Loaded MineSkin limits: interval={0}ms, concurrency={1}",
                            new Object[]{intervalMillis.get(), concurrency.get()});
                })
                .exceptionally(throwable -> {
                    // Suppress noisy stack traces. Just log a clean info message without disrupting server
                    LOGGER.log(Level.INFO, "[FancyTags] MineSkin API is currently unreachable to fetch account limits ({0}); using default rate limits.", throwable.getMessage());
                    return null;
                })
                .whenComplete((v, t) -> isRefreshing.set(false));
    }

    @Override
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    @Override
    public int intervalMillis() {
        int effective = intervalMillis.get() + currentPenalty();
        return Math.min(effective, MAX_EFFECTIVE_INTERVAL_MILLIS);
    }

    @Override
    public int concurrency() {
        return concurrency.get();
    }

    @Override
    public void reportFailure() {
        int penalty;
        int baseline;
        synchronized (this) {
            int existing = currentPenalty();
            penalty = Math.min(MAX_PENALTY_MILLIS, existing + FAILURE_STEP_MILLIS);
            peakPenaltyMillis.set(penalty);
            lastFailureAt.set(System.currentTimeMillis());
            baseline = intervalMillis.get();
        }
        int effective = Math.min(baseline + penalty, MAX_EFFECTIVE_INTERVAL_MILLIS);
        LOGGER.log(Level.FINE, "[FancyTags] Throttling MineSkin queue after failure: effective {0}ms", effective);
    }

    private int currentPenalty() {
        int peak = peakPenaltyMillis.get();
        if (peak == 0) return 0;
        long elapsed = System.currentTimeMillis() - lastFailureAt.get();
        if (elapsed <= 0) return peak;
        long decayed = peak - (elapsed * DECAY_MILLIS_PER_SECOND / 1000);
        return (int) Math.max(0, decayed);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
