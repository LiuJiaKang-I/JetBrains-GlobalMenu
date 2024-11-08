package io.gitlab.jfronny.globalmenu.reflect

import io.gitlab.jfronny.commons.unsafe.reflect.Reflect
import java.awt.Component
import java.awt.Insets

sealed interface Peer {
    val nativePtr: Long

    class X11(private val inner: Any) : Peer {
        init {
            componentPeerClass.cast(inner)
        }
        override val nativePtr: Long get() = getPtrMethod(inner)

        companion object {
            val componentPeerClass = Class.forName("sun.awt.X11.XComponentPeer")
            private val getPtrMethod = Reflect.instanceFunction(componentPeerClass, "getWindow", Long::class.java).unchecked
        }
    }
    class WL(private val inner: Any) : Peer {
        init {
            componentPeerClass.cast(inner)
        }
        override val nativePtr: Long get() = nativePtrField.getLong(inner)
        fun performLocked(runnable: Runnable) {
            performLockedMethod(inner, runnable)
        }
        var decorated: Boolean
            get() = containerPeerClass.isInstance(inner) && getInsetsMethod(inner).top > 0
            set(value) {
                val decoration = decorationField.get(inner)
                isUndecoratedField.setBoolean(decoration, !value)
                markRepaintNeededMethod(decoration)
            }

        companion object {
            val componentPeerClass = Class.forName("sun.awt.wl.WLComponentPeer")
            private val performLockedMethod = Reflect.instanceProcedure(componentPeerClass, "performLocked", Runnable::class.java).unchecked1
            private val nativePtrField = componentPeerClass.getDeclaredField("nativePtr").withAccess
            private val containerPeerClass = Class.forName("java.awt.peer.ContainerPeer")
            private val getInsetsMethod = Reflect.instanceFunction(containerPeerClass, "getInsets", Insets::class.java).unchecked
            private val decoratedPeerClass = Class.forName("sun.awt.wl.WLDecoratedPeer")
            private val decorationField = decoratedPeerClass.getDeclaredField("decoration").withAccess
            private val frameDecorationClass = Class.forName("sun.awt.wl.WLFrameDecoration")
            private val markRepaintNeededMethod = Reflect.instanceProcedure(frameDecorationClass, "markRepaintNeeded").unchecked
            private val isUndecoratedField = frameDecorationClass.getDeclaredField("isUndecorated").withAccess
        }
    }

    companion object {
        operator fun invoke(peer: Any): Peer {
            if (peer.javaClass.name.contains("X11")) return X11(peer)
            if (peer.javaClass.name.contains("WL")) return WL(peer)
            throw IllegalArgumentException("Unknown peer type: ${peer.javaClass}")
        }
    }
}

private val peerField = Component::class.java.getDeclaredField("peer").apply { isAccessible = true }

val Component.peer: Peer get() = Peer(peerField.get(this))