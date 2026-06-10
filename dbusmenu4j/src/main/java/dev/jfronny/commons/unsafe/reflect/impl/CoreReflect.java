package dev.jfronny.commons.unsafe.reflect.impl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * Minimal inline version of dev.jfronny.commons.unsafe.reflect.impl.CoreReflect for Java 21 compatibility.
 * Uses sun.misc.Unsafe to bypass module access checks for IMPL_LOOKUP.
 */
public class CoreReflect {
    private static final MethodHandles.Lookup IMPL_LOOKUP;

    static {
        MethodHandles.Lookup lookup;
        try {
            // Use sun.misc.Unsafe to read the IMPL_LOOKUP static field,
            // bypassing Java module access checks.
            sun.misc.Unsafe unsafe = getUnsafe();
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long fieldOffset = unsafe.staticFieldOffset(implLookupField);
            Object base = unsafe.staticFieldBase(implLookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(base, fieldOffset);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to obtain IMPL_LOOKUP", t);
        }
        IMPL_LOOKUP = lookup;
    }

    private static sun.misc.Unsafe getUnsafe() {
        // sun.misc.Unsafe.getUnsafe() checks the caller classloader,
        // but we can bypass that by using reflection to read the field.
        // In IntelliJ's plugin classloader environment, setAccessible on
        // sun.misc.Unsafe.theUnsafe typically works because the IDE adds
        // the necessary --add-opens flags at startup.
        try {
            Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            return (sun.misc.Unsafe) theUnsafeField.get(null);
        } catch (Exception e) {
            // Last resort: allocate an Unsafe via reflection
            // This works on most JVMs but is not officially supported
            try {
                var ctor = sun.misc.Unsafe.class.getDeclaredConstructor();
                ctor.setAccessible(true);
                return ctor.newInstance();
            } catch (Exception e2) {
                throw new RuntimeException("Cannot obtain sun.misc.Unsafe instance", e2);
            }
        }
    }

    public static MethodHandles.Lookup lookup(Class<?> klazz) throws IllegalAccessException {
        return MethodHandles.privateLookupIn(klazz, IMPL_LOOKUP);
    }
}
