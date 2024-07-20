package io.gitlab.jfronny.globalmenu.proxy

interface Menu {
    val id: Int
    val isSeparator: Boolean
    val label: String
    val isEnabled: Boolean
    val isVisible: Boolean
    val iconData: ByteArray?
    val shortcut: Array<String>?
    val toggleType: String?
    val toggleState: Int
    val children: List<Menu>?
    fun onEvent()
    fun update()
}