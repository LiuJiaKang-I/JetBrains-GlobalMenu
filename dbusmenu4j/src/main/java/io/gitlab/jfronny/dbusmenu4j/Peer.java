package io.gitlab.jfronny.dbusmenu4j;

import io.gitlab.jfronny.commons.unsafe.reflect.Reflect;
import io.gitlab.jfronny.commons.unsafe.reflect.impl.CoreReflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.function.*;

public sealed interface Peer {
    long nativePointer();

    static Peer coerce(Object peer) {
        if (peer.getClass().getName().contains("X11")) return new X11(peer);
        if (peer.getClass().getName().contains("WL")) return new WL(peer);
        throw new IllegalArgumentException("Unsupported peer type: " + peer.getClass());
    }

    final class X11 implements Peer {
        private static final Class<?> klazz;
        private static final Method getPtrMethod;

        static {
            try {
                klazz = Class.forName("sun.awt.X11.XBaseWindow");
                getPtrMethod = klazz.getDeclaredMethod("getWindow");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                Resolver.logger.error("Failed to resolve X11 peer methods", e);
                throw new RuntimeException(e);
            }
        }

        private final Object peer;

        public X11(Object peer) {
            this.peer = klazz.cast(peer);
        }

        @Override
        public long nativePointer() {
            try {
                return (Long) getPtrMethod.invoke(peer);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    final class WL implements Peer {
        private static final Class<?> klazz;
        private static final Class<?> containerPeerClass;
        private static final Function<Object, Insets> getInsetsMethod;
        private static final Field nativePtrField;
        private static final BiConsumer<Object, Runnable> performLockedMethod;
        private static final Field wlSurfaceField;
        private static final Field decorationField;
        private static final Consumer<Object> markRepaintNeededMethod;
        private static final Field isUndecoratedField;
        private static final Function<Object, Long> getWlSurfacePtr;

        private final Object peer;

        static {
            try {
                klazz = Class.forName("sun.awt.wl.WLComponentPeer");
                nativePtrField = Resolver.withAccess(klazz.getDeclaredField("nativePtr"));
                performLockedMethod = Resolver.uncheck(Reflect.instanceProcedure(klazz, "performLocked", Runnable.class));
                wlSurfaceField = Resolver.withAccess(klazz.getDeclaredField("wlSurface"));
                containerPeerClass = Class.forName("java.awt.peer.ContainerPeer");
                getInsetsMethod = Resolver.uncheck(Reflect.instanceFunction(containerPeerClass, "getInsets", Insets.class));
                Class<?> decoratedPeerClass = Class.forName("sun.awt.wl.WLDecoratedPeer");
                decorationField = Resolver.withAccess(decoratedPeerClass.getDeclaredField("decoration"));
                Class<?> frameDecorationClass = Class.forName("sun.awt.wl.WLFrameDecoration");
                markRepaintNeededMethod = Resolver.uncheck(Reflect.instanceProcedure(frameDecorationClass, "markRepaintNeeded"));
                isUndecoratedField = Resolver.withAccess(frameDecorationClass.getDeclaredField("isUndecorated"));
                Class<?> wlSurfaceClass = Class.forName("sun.awt.wl.WLMainSurface");
                getWlSurfacePtr = Resolver.uncheck(Reflect.instanceFunction(wlSurfaceClass, "getWlSurfacePtr", long.class));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        public WL(Object peer) {
            this.peer = klazz.cast(peer);
        }

        public void performLocked(Runnable runnable) {
            performLockedMethod.accept(peer, runnable);
        }

        public void setDecorated(boolean decorated) {
            try {
                Object decoration = decorationField.get(peer);
                isUndecoratedField.setBoolean(decoration, !decorated);
                markRepaintNeededMethod.accept(decoration);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isDecorated() {
            return containerPeerClass.isInstance(peer) && getInsetsMethod.apply(peer).top > 0;
        }

        @Override
        public long nativePointer() {
            try {
                return nativePtrField.getLong(peer);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public long getSurfacePtr() {
            try {
                return getWlSurfacePtr.apply(wlSurfaceField.get(peer));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Resolver {
        private static final Logger logger = LoggerFactory.getLogger("dbusmenu4j/PeerResolver");
        private static final Field peerField;

        private static final MethodHandle accessibleSetter;

        static {
            try {
                peerField = Component.class.getDeclaredField("peer");
                peerField.setAccessible(true);
                accessibleSetter = CoreReflect.lookup(AccessibleObject.class).findSetter(AccessibleObject.class, "override", boolean.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error("Failed to resolve peer field", e);
                throw new RuntimeException(e);
            }
        }

        private static Field withAccess(Field field) throws Throwable {
            accessibleSetter.invoke(field, true);
            return field;
        }

        private static Consumer<Object> uncheck(Consumer<?> in) {
            return (Consumer<Object>) in;
        }

        private static <R> Function<Object, R> uncheck(Function<?, R> in) {
            return (Function<Object, R>) in;
        }

        private static <T, R> BiFunction<Object, T, R> uncheck(BiFunction<?, T, R> in) {
            return (BiFunction<Object, T, R>) in;
        }

        private static <T> BiConsumer<Object, T> uncheck(BiConsumer<?, T> in) {
            return (BiConsumer<Object, T>) in;
        }

        public static Peer resolve(Component component) throws IllegalAccessException {
            return coerce(peerField.get(component));
        }
    }
}