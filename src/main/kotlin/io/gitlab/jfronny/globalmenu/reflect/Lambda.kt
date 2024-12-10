package io.gitlab.jfronny.globalmenu.reflect

import java.util.function.*
import java.util.function.Function

val <T, R> Function<T, R>.unchecked: Function<Any, R> get() = Function { it: Any -> apply(it as T) }
val <T1, T2> BiConsumer<T1, T2>.unchecked1: BiConsumer<Any, T2> get() = BiConsumer { t1: Any, t2: T2 -> accept(t1 as T1, t2) }
operator fun Runnable.invoke() = run()
operator fun <T> Supplier<T>.invoke() = get()
operator fun <T, R> Function<T, R>.invoke(t: T): R = apply(t)
operator fun <T1, T2, R> BiFunction<T1, T2, R>.invoke(t1: T1, t2: T2): R = apply(t1, t2)
operator fun <T1, T2> BiConsumer<T1, T2>.invoke(t1: T1, t2: T2) = accept(t1, t2)
operator fun <T> Consumer<T>.invoke(t: T) = accept(t)