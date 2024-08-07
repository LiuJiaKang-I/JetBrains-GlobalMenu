package io.gitlab.jfronny.globalmenu

import io.gitlab.jfronny.commons.MultiConsumer

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