package com.example.ordersservice.config;

import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.github.resilience4j.retry.event.RetryEvent;
import io.github.resilience4j.retry.event.RetryOnRetryEvent;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Resilience4jEventLogger {

    private static final Logger log = LoggerFactory.getLogger(Resilience4jEventLogger.class);

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    public Resilience4jEventLogger(CircuitBreakerRegistry cbRegistry, RetryRegistry retryRegistry) {
        this.cbRegistry = cbRegistry;
        this.retryRegistry = retryRegistry;
    }

    @PostConstruct
    public void init() {
        // CircuitBreaker događaji
        cbRegistry.getAllCircuitBreakers().forEach(cb ->
                cb.getEventPublisher()
                        .onStateTransition(this::onStateTransition)
                        .onError(this::onError)
                        .onSuccess(this::onSuccess)
        );

        // Retry događaji
        retryRegistry.getAllRetries().forEach(retry ->
                retry.getEventPublisher()
                        .onRetry(this::onRetry)
                        .onSuccess(this::onRetrySuccess)
        );
    }

    // CircuitBreaker logovi
    private void onStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        log.warn("🛡️ CircuitBreaker '{}' STATE: {} → {}",
                event.getCircuitBreakerName(),
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState());
    }

    private void onError(CircuitBreakerEvent event) {
        log.error("💥 CircuitBreaker '{}' ERROR: {}",
                event.getCircuitBreakerName(),
                event.getEventType());
    }

    private void onSuccess(CircuitBreakerEvent event) {
        log.info("✅ CircuitBreaker '{}' SUCCESS: call succeeded",
                event.getCircuitBreakerName());
    }

    // Retry logovi
    private void onRetry(RetryOnRetryEvent event) {
        log.warn("🔁 Retry '{}': attempt {} due to {}",
                event.getName(),
                event.getNumberOfRetryAttempts(),
                event.getLastThrowable() != null ? event.getLastThrowable().getMessage() : "unknown error");
    }

    private void onRetrySuccess(RetryEvent event) {
        log.info("🎯 Retry '{}' succeeded after {} attempts",
                event.getName(),
                event.getNumberOfRetryAttempts());
    }
}