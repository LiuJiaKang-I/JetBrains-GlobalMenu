package io.gitlab.jfronny.globalmenu.proxy

import com.intellij.openapi.actionSystem.impl.ActionMenu
import com.intellij.openapi.application.EDT
import io.gitlab.jfronny.dbusmenu4j.Menu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.swing.JMenuItem

class SwingRootMenu(private var menuItems: List<JMenuItem>?, private val name: String, private val holder: SwingMenuHolder): Menu.Abstract() {
    override fun getId() = 0
    override fun isSeparator() = menuItems == null
    override fun getLabel() = name
    override fun isEnabled() = true
    override fun isVisible() = true
    override fun getIconData() = null
    override fun getShortcut() = null
    override fun getToggleType() = null
    override fun getToggleState() = 0
    private var _children: List<Menu>? = null
    override fun getChildren() = _children

    override fun onEvent() {
    }

    override fun update() {
        super.update()
        runBlocking {
            launch(Dispatchers.EDT) {
                syncChildren()
            }
        }
    }

    fun update(items: List<ActionMenu>) {
        menuItems = items
        syncChildren()
    }

    init {
        syncChildren()
    }

    private fun syncChildren() {
        _children = menuItems?.map { SwingMenu(it, holder).apply { syncChildren(2) } } // setting this to 2 may help prevent missing entries but is SLLOOOOWWW
    }
}
