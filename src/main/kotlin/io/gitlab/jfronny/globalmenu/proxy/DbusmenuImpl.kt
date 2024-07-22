package io.gitlab.jfronny.globalmenu.proxy

import com.canonical.*
import io.gitlab.jfronny.globalmenu.DPair
import io.gitlab.jfronny.globalmenu.GlobalMenu
import org.freedesktop.dbus.types.UInt32
import org.freedesktop.dbus.types.Variant

class DbusmenuImpl(windowId: Long, private val menuHolder: MenuHolder) : Dbusmenu {
    private val menuPath: String = getMenuPath(windowId)
    override fun getObjectPath(): String = menuPath
//    override fun getVersion(): UInt32 = UInt32(3)
//    override fun getTextDirection(): String = "none"
//    override fun getStatus(): String = "normal"
//
//    override fun getIconThemePath(): Dbusmenu.PropertyIconThemePathType =
//        object : Dbusmenu.PropertyIconThemePathType, List<String> by listOf() {}

    override fun GetLayout(parentId: Int, recursionDepth: Int, propertyNames: MutableList<String>?): DPair<UInt32, GetLayoutStruct> {
        try {
            return DPair(
                UInt32(parentId.toUInt().toLong()),
                getLayout(parentId, recursionDepth, propertyNames, menuHolder.find(parentId)!!)
            )
        } catch (e: Exception) {
            GlobalMenu.Log.error("Failed to get layout for menu $parentId", e)
            throw e
        }
    }

    private fun getLayout(parentId: Int, recursionDepth: Int, propertyNames: MutableList<String>?, menu: Menu): GetLayoutStruct {
        val properties = readProperties(menu)
        val children = mutableListOf<Variant<*>>()

        menu.children?.let {
            for (sm in it) {
                children.add(Variant(getLayout(parentId, recursionDepth, propertyNames, sm)))
            }
            properties["children-display"] = Variant("submenu")
        }

        return GetLayoutStruct(menu.id, properties, children)
    }

    override fun GetGroupProperties(
        ids: MutableList<Int>,
        propertyNames: MutableList<String>?
    ): MutableList<GetGroupPropertiesStruct> = mutableListOf<GetGroupPropertiesStruct>().apply {
        ids.forEach { id ->
            menuHolder.find(id)?.let { menu ->
                add(GetGroupPropertiesStruct(id, readProperties(menu)))
            }
        }
    }

    override fun GetProperty(id: Int, name: String?): Variant<*>? = menuHolder.find(id)?.let { menu -> readProperties(menu)[name] }

    private fun readProperties(menu: Menu): MutableMap<String, Variant<*>> {
        if (menu.isSeparator) return mutableMapOf("type" to Variant("separator"))
        val properties = mutableMapOf<String, Variant<*>>()
        properties["type"] = Variant("standard")
        properties["label"] = Variant(menu.label)
        properties["visible"] = Variant(menu.isVisible)
        properties["enabled"] = Variant(menu.isEnabled)
        if (!menu.shortcut.isNullOrEmpty()) properties["shortcut"] = Variant(menu.shortcut)
        if (!menu.toggleType.isNullOrEmpty()) {
            properties["toggle-type"] = Variant(menu.toggleType)
            properties["toggle-state"] = Variant(menu.toggleState)
        }
        if (menu.iconData?.isNotEmpty() == true) properties["icon-data"] = Variant(menu.iconData)
        return properties
    }

    override fun Event(id: Int, eventId: String, data: Variant<*>?, timestamp: UInt32?) {
        GlobalMenu.Log.warn("Event $eventId for menu $id (${menuHolder.find(id)})")
        try {
            when (eventId) {
                "clicked" -> menuHolder.find(id)?.onEvent()
                "opened" -> menuHolder.find(id)?.update()
            }
        } catch (e: Exception) {
            GlobalMenu.Log.error("Failed to handle event $eventId for menu $id", e)
            throw e
        }
    }

    override fun EventGroup(events: MutableList<EventGroupStruct>?): MutableList<Int>? = null // not needed?
    override fun AboutToShow(id: Int): Boolean {
        GlobalMenu.Log.warn("About to show menu $id")
        try {
            menuHolder.find(id)?.update()
            return true
        } catch (e: Exception) {
            GlobalMenu.Log.error("Failed to update menu $id", e)
            throw e
        }

    }
    override fun AboutToShowGroup(ids: MutableList<Int>?): DPair<MutableList<Int>, MutableList<Int>>? = null // not needed?

    companion object {
        fun getMenuPath(windowId: Long): String = "/com/canonical/menu0x${windowId.toString(16)}"
    }
}
