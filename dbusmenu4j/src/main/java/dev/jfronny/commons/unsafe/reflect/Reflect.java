package dev.jfronny.commons.unsafe.reflect;

import dev.jfronny.commons.unsafe.reflect.impl.CoreReflect;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.function.*;

/**
 * Minimal inline version of dev.jfronny.commons.unsafe.reflect.Reflect for Java 21 compatibility.
 * Only includes methods actually used by the globalmenu plugin.
 */
@SuppressWarnings("unchecked")
public class Reflect {
    private static final MethodHandle accessibleSetter;

    static {
        try {
            accessibleSetter = CoreReflect.lookup(AccessibleObject.class)
                    .findSetter(AccessibleObject.class, "override", boolean.class);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize Reflect", e);
        }
    }

    /**
     * Creates a BiConsumer that calls an instance method with one parameter (void return).
     */
    public static <TTarget, TParam> BiConsumer<TTarget, TParam> instanceProcedure(Class<TTarget> targetClass, String name, Class<TParam> parameterType) throws Throwable {
        MethodHandles.Lookup lookup = CoreReflect.lookup(targetClass);
        MethodHandle handle = lookup.unreflect(getMethod(targetClass, name, parameterType));
        return (BiConsumer<TTarget, TParam>) LambdaMetafactory.metafactory(
                lookup,
                "accept",
                MethodType.methodType(BiConsumer.class),
                MethodType.methodType(Void.TYPE, Object.class, Object.class),
                handle,
                MethodType.methodType(Void.TYPE, targetClass, boxType(parameterType))
        ).getTarget().invoke();
    }

    /**
     * Creates a Function that calls an instance method with no parameters (non-void return).
     */
    public static <TTarget, TOut> Function<TTarget, TOut> instanceFunction(Class<TTarget> targetClass, String name, Class<TOut> returnType) throws Throwable {
        MethodHandles.Lookup lookup = CoreReflect.lookup(targetClass);
        MethodHandle handle = lookup.unreflect(getMethod(targetClass, name));
        return (Function<TTarget, TOut>) LambdaMetafactory.metafactory(
                lookup,
                "apply",
                MethodType.methodType(Function.class),
                MethodType.methodType(Object.class, Object.class),
                handle,
                MethodType.methodType(returnType, targetClass)
        ).getTarget().invoke();
    }

    /**
     * Creates a Supplier that calls a static method with no parameters (non-void return).
     */
    public static <TOut> Supplier<TOut> staticFunction(Class<?> targetClass, String name, Class<TOut> returnType) throws Throwable {
        MethodHandles.Lookup lookup = CoreReflect.lookup(targetClass);
        MethodHandle handle = lookup.unreflect(getMethod(targetClass, name));
        return (Supplier<TOut>) LambdaMetafactory.metafactory(
                lookup,
                "get",
                MethodType.methodType(Supplier.class),
                MethodType.methodType(Object.class),
                handle,
                MethodType.methodType(returnType)
        ).getTarget().invoke();
    }

    public static void setAccessible(Field field) throws Throwable {
        accessibleSetter.invoke(field, true);
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = findMethod(clazz, true, name, parameterTypes);
        if (method == null) throw new NoSuchMethodException(clazz.getName() + "." + name);
        return method;
    }

    private static Method findMethod(Class<?> clazz, boolean withPrivate, String name, Class<?>... parameterTypes) {
        if (clazz == null) return null;
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equals(name)) continue;
            if (method.getParameterCount() != parameterTypes.length) continue;
            boolean paramsMatch = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!method.getParameterTypes()[i].isAssignableFrom(parameterTypes[i])) {
                    paramsMatch = false;
                    break;
                }
            }
            if (!paramsMatch) continue;
            if (!withPrivate && Modifier.isPrivate(method.getModifiers())) continue;
            return method;
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            Method method = findMethod(iface, false, name, parameterTypes);
            if (method != null) return method;
        }
        return findMethod(clazz.getSuperclass(), false, name, parameterTypes);
    }

    private static Class<?> boxType(Class<?> type) {
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        return type;
    }
}
