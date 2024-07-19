package io.gitlab.jfronny.globalmenu

import io.gitlab.jfronny.commons.unsafe.reflect.Reflect
import io.gitlab.jfronny.commons.unsafe.reflect.impl.CoreReflect
import java.awt.Component
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field

private val accessibleSetter = CoreReflect.lookup(AccessibleObject::class.java).findSetter(AccessibleObject::class.java, "override", Boolean::class.java)
private fun setAccessible(field: Field) {
    accessibleSetter.invoke(field, true)
}

private val peerField = Component::class.java.getDeclaredField("peer").apply { isAccessible = true }

val Component.peer: Peer get() = Peer(peerField.get(this))

private val componentPeerClass = Class.forName("sun.awt.wl.WLComponentPeer")
private val performLockedMethod = Reflect.instanceProcedure(componentPeerClass, "performLocked", Runnable::class.java).unchecked1
private val nativePtrField = componentPeerClass.getDeclaredField("nativePtr").apply { setAccessible(this) }

class Peer(private val inner: Any) {
    val nativePtr: Long get() = nativePtrField.getLong(inner)
    fun performLocked(runnable: Runnable) {
        performLockedMethod(inner, runnable)
    }
    fun registerCleaner(runnable: Runnable) {
        GlobalMenu.Cleaner.register(this, runnable)
    }
}

private val wlDisplayClass = Class.forName("sun.awt.wl.WLDisplay")
private val getInstanceMethod = Reflect.staticFunction(wlDisplayClass, "getInstance", wlDisplayClass)
private val getDisplayPtrMethod = Reflect.instanceFunction(wlDisplayClass, "getDisplayPtr", Long::class.java).unchecked
fun getDisplayPtr(): Long {
    val display = getInstanceMethod()
    return getDisplayPtrMethod(display) as Long
}