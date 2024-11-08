package io.gitlab.jfronny.globalmenu.reflect

import io.gitlab.jfronny.commons.unsafe.reflect.impl.CoreReflect
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field

private val accessibleSetter = CoreReflect.lookup(AccessibleObject::class.java).findSetter(AccessibleObject::class.java, "override", Boolean::class.java)
val Field.withAccess: Field get() {
    accessibleSetter.invoke(this, true)
    return this
}
