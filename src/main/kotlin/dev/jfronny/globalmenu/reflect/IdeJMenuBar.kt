package dev.jfronny.globalmenu.reflect

import com.intellij.openapi.actionSystem.impl.ActionMenu
import dev.jfronny.commons.unsafe.reflect.Reflect
import javax.swing.JMenuBar

private val bar = Class.forName("com.intellij.platform.ide.menu.IdeJMenuBar")
private val addUpdateGlobalMenuRootsListener = Reflect.instanceProcedure(bar, "addUpdateGlobalMenuRootsListener", Runnable::class.java).unchecked1
private val getRootMenuItems = Reflect.instanceFunction(bar, "getRootMenuItems", List::class.java).unchecked

fun JMenuBar.maybeAddUpdateListener(runnable: (List<ActionMenu>) -> Unit) {
    if (bar.isInstance(this)) addUpdateGlobalMenuRootsListener(this, Runnable {
        runnable(getRootMenuItems(this) as List<ActionMenu>)
    })
}