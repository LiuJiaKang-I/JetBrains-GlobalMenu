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
    fun maybeUpdate()

    abstract class Abstract : Menu {
        private var lastUpdated = 0L
        override fun maybeUpdate() {
            if (System.currentTimeMillis() - lastUpdated > 1000) {
                update()
            }
        }

        override fun update() {
            lastUpdated = System.currentTimeMillis()
        }
    }
}