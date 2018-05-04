package org.testcontainers.containers.wait.strategy;

import lombok.NonNull;
import org.rnorth.ducttape.ratelimits.RateLimiter;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class AbstractWaitStrategy implements WaitStrategy {

    protected WaitStrategyTarget waitStrategyTarget;

    @NonNull
    protected Duration startupTimeout = Duration.ofSeconds(60);

    private static final RateLimiter DOCKER_CLIENT_RATE_LIMITER = RateLimiterBuilder
        .newBuilder()
        .withRate(1, TimeUnit.SECONDS)
        .withConstantThroughput()
        .build();

    /**
     * Wait until the target has started.
     *
     * @param waitStrategyTarget the target of the WaitStrategy
     */
    @Override
    public void waitUntilReady(WaitStrategyTarget waitStrategyTarget) {
        this.waitStrategyTarget = waitStrategyTarget;
        waitUntilReady();
    }

    /**
     * Wait until {@link #waitStrategyTarget} has started.
     */
    protected abstract void waitUntilReady();

    /**
     * Set the duration of waiting time until container treated as started.
     *
     * @param startupTimeout timeout
     * @return this
     * @see WaitStrategy#waitUntilReady(WaitStrategyTarget)
     */
    public WaitStrategy withStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
        return this;
    }

    /**
     * @return the ports on which to check if the container is ready
     */
    protected Set<Integer> getLivenessCheckPorts() {
        return waitStrategyTarget.getLivenessCheckPortNumbers();
    }

    /**
     * @return the rate limiter to use
     */
    protected RateLimiter getRateLimiter() {
        return DOCKER_CLIENT_RATE_LIMITER;
    }
}
