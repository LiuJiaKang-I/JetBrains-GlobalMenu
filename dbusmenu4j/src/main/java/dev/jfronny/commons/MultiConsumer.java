package dev.jfronny.commons;

import java.util.function.Consumer;

/**
 * Minimal inline version of dev.jfronny.commons.MultiConsumer for Java 21 compatibility.
 */
@FunctionalInterface
public interface MultiConsumer<T> extends Consumer<T> {
}
