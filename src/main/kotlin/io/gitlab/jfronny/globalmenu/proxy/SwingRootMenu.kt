package io.gitlab.jfronny.globalmenu.proxy

import com.intellij.openapi.actionSystem.impl.ActionMenu
import com.intellij.openapi.application.EDT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.swing.JMenuItem

class SwingRootMenu(private var menuItems: List<JMenuItem>?, private val name: String, private val holder: SwingMenuHolder): Menu.Abstract() {
    override val id: Int get() = 0
    override val isSeparator: Boolean get() = menuItems == null
    override val label: String get() = name
    override val isEnabled: Boolean get() = true
    override val isVisible: Boolean get() = true
    override val iconData: ByteArray? get() = null
    override val shortcut: Array<String>? get() = null
    override val toggleType: String? get() = null
    override val toggleState: Int get() = 0
    private var _children: List<Menu>? = null
    override val children: List<Menu>? get() = _children

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
        _children = menuItems?.map { SwingMenu(it, holder).apply { syncChildren(1) } } // setting this to 2 may help prevent missing entries but is SLLOOOOWWW
    }
}
