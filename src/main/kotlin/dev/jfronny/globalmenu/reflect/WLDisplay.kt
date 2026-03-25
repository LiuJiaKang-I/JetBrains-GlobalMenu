package dev.jfronny.globalmenu.reflect

import dev.jfronny.commons.unsafe.reflect.Reflect

private val wlDisplayClass = Class.forName("sun.awt.wl.WLDisplay")
private val getInstanceMethod = Reflect.staticFunction(wlDisplayClass, "getInstance", wlDisplayClass)
private val getDisplayPtrMethod = Reflect.instanceFunction(wlDisplayClass, "getDisplayPtr", Long::class.java).unchecked
fun getDisplayPtr(): Long {
    val display = getInstanceMethod()
    return getDisplayPtrMethod(display) as Long
}