package io.gitlab.jfronny.globalmenu.proxy

import com.intellij.openapi.actionSystem.impl.ActionMenu
import com.intellij.openapi.actionSystem.impl.ActionMenuItem
import io.gitlab.jfronny.dbusmenu4j.Menu
import io.gitlab.jfronny.dbusmenu4j.MenuHolder
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class SwingMenuHolder(bar: JMenuBar, menuName: String): MenuHolder {
    private val root = SwingRootMenu((0 until bar.menuCount).map { bar.getMenu(it) }, menuName, this)

    override fun find(menuId: Int): Menu? {
        if (menuId == 0) return root
        return find(root, menuId)
    }

    private fun find(parent: Menu, menuId: Int): Menu? {
        return parent.children?.let { children ->
            for (child in children) {
                if (child.id == menuId) return child
                val found = find(child, menuId)
                if (found != null) return found
            }
            null
        }
    }

    fun getId(menuItem: JMenuItem?): Int = when (menuItem) {
        is ActionMenu -> menuItem.anAction.toString().hashCode()
        is ActionMenuItem -> menuItem.anAction.toString().hashCode()
        else -> System.identityHashCode(menuItem)
    }
    fun update(items: List<ActionMenu>) = root.update(items)
}