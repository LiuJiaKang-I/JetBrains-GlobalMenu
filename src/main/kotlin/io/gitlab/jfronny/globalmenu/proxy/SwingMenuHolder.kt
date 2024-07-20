package io.gitlab.jfronny.globalmenu.proxy

import javax.swing.JMenuBar
import javax.swing.JMenuItem

class SwingMenuHolder(bar: JMenuBar, menuName: String): MenuHolder {
    private val root = SwingRootMenu((0 until bar.menuCount).map { bar.getMenu(it) }, menuName, this)

    override fun find(menuId: Int): Menu? {
        if (menuId == 0) return root
        return find(root, menuId)
    }

    private fun find(parent: Menu, menuId: Int): Menu? {
        parent.children?.forEach {
            if (it.id == menuId) return it
            val found = find(it, menuId)
            if (found != null) return found
        }
        return null
    }

    fun getId(menuItem: JMenuItem?): Int {
        return System.identityHashCode(menuItem)
    }
}