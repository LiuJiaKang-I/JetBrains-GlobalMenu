package dev.jfronny.globalmenu.proxy

import com.intellij.openapi.actionSystem.impl.ActionMenu
import com.intellij.openapi.actionSystem.impl.ActionMenuItem
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import dev.jfronny.dbusmenu4j.Menu
import dev.jfronny.globalmenu.GlobalMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.swing.*

class ActionMenu(menuItem: JMenuItem?, id: Int, holder: ActionMenuHolder) : dev.jfronny.dbusmenu4j.SwingMenu<ActionRootMenu, ActionMenuHolder>(menuItem, id, holder) {
    constructor(menuItem: JMenuItem, holder: ActionMenuHolder) : this(menuItem, holder.getId(menuItem), holder)
    constructor(id: Int, holder: ActionMenuHolder) : this(null, id, holder)

    override fun getToggleType() = if (menuItem is ActionMenuItem) {
        //TODO handle action items
        if ((menuItem as ActionMenuItem).isToggleable) "checkmark"
        else null
    } else super.getToggleType()
    private var _children: List<Menu>? = null
    override fun getChildren() = _children

    override fun onEvent() {
        if (menuItem == null) return
        ApplicationManager.getApplication().invokeLater(menuItem!!::doClick)
        when (menuItem) {
            is ActionMenuItem -> {}
            is ActionMenu -> {}
            else -> super.onEvent()
        }
    }

    override fun update() {
        super.update()
        try {
            if (menuItem is ActionMenu) {
                val am = menuItem as ActionMenu
                runBlocking {
                    launch(Dispatchers.EDT) {
                        am.removeAll()
                        am.isSelected = true
                        am.fillMenu()
                        syncChildren(2)
                    }
                }
            }
        } catch (e: Exception) {
            GlobalMenu.Log.error("Failed to update menu", e)
            throw e
        }
    }

    override fun syncChildren(depth: Int) {
//        GlobalMenu.Log.warn("Syncing children for $label")
        _children = when (menuItem) {
            is ActionMenu -> {
                val ch = mutableListOf<Menu>()
                val am = menuItem as ActionMenu
                for (each in am.popupMenu.components) {
                    if (each == null) continue
                    if (each is JSeparator) {
                        ch.add(ActionMenu(System.identityHashCode(each), holder))
                        continue
                    }
                    if (each !is JMenuItem) {
                        continue
                    }
                    val cmi = ActionMenu(each, holder)
                    if (depth > 1) {
                        if (each is ActionMenu) {
                            each.removeAll()
                            each.isSelected = true
                            each.fillMenu() // This is REALLY expensive and on the EDT TODO: find out whether we can avoid it
                        }
                        cmi.syncChildren(depth - 1)
                    }
                    ch.add(cmi)
                }
                ch
            }
            is JMenu -> {
                super.syncChildren(depth)
                children
            }
            else -> null
        }
    }
}