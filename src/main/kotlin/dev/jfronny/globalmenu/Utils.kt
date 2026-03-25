package dev.jfronny.globalmenu

import dev.jfronny.commons.MultiConsumer
import java.awt.Dimension
import java.awt.Window

@PublishedApi internal class Node<T>(val value: T, var next: Node<T>? = null)
inline fun <reified T> buildArray(generate: MultiConsumer<T>.() -> Unit): Array<T> {
    var length = 0
    var head: Node<T>? = null
    MultiConsumer<T> { value ->
        head = Node(value, head)
        length++
    }.generate()
    val array = arrayOfNulls<T>(length)
    while (head != null) {
        array[--length] = head!!.value
        head = head!!.next
    }
    @Suppress("UNCHECKED_CAST")
    return array as Array<T>
}

fun Window.forceRedraw() {
    size = (size + Dimension(1, 0)).let { if (isMinimumSizeSet) max(it, minimumSize) else it }
}

private operator fun Dimension.plus(dimension: Dimension): Dimension =
    Dimension(width + dimension.width, height + dimension.height)
private fun max(a: Dimension, b: Dimension): Dimension =
    Dimension(maxOf(a.width, b.width), maxOf(a.height, b.height))