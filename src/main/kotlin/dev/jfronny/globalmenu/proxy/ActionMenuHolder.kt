package dev.jfronny.globalmenu.proxy

import com.intellij.openapi.actionSystem.impl.ActionMenu
import com.intellij.openapi.actionSystem.impl.ActionMenuItem
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class ActionMenuHolder(bar: JMenuBar, menuName: String): dev.jfronny.dbusmenu4j.SwingMenuHolder<ActionRootMenu>(null) {
    init {
        root = ActionRootMenu(bar, menuName, this)
    }

    override fun getId(menuItem: JMenuItem?): Int = when (menuItem) {
        is ActionMenu -> menuItem.anAction.toString().hashCode()
        is ActionMenuItem -> menuItem.anAction.toString().hashCode()
        else -> super.getId(menuItem)
    }
    fun update(items: List<ActionMenu>) = root.update(items)
}