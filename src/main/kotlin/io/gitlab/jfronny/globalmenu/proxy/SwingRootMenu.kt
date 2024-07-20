package io.gitlab.jfronny.globalmenu.proxy

import javax.swing.JMenuItem

class SwingRootMenu(private val menuItems: List<JMenuItem>?, private val name: String, private val holder: SwingMenuHolder): Menu {
    override val id: Int get() = 0
    override val isSeparator: Boolean get() = menuItems == null
    override val label: String get() = name
    override val isEnabled: Boolean get() = true
    override val isVisible: Boolean get() = true
    override val iconData: ByteArray? get() = null
    override val shortcut: Array<String>? get() = null
    override val toggleType: String? get() = null
    override val toggleState: Int get() = 0
    override val children: List<Menu>? get() = menuItems?.map { SwingMenu(it, holder) }

    override fun onEvent() {
    }
}
