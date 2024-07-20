package io.gitlab.jfronny.globalmenu.proxy

import com.canonical.*
import org.freedesktop.dbus.types.UInt32
import org.freedesktop.dbus.types.Variant

class DbusmenuImpl : Dbusmenu {
    override fun getObjectPath(): String {
        TODO("Not yet implemented")
    }

    override fun getVersion(): UInt32 {
        TODO("Not yet implemented")
    }

    override fun getTextDirection(): String {
        TODO("Not yet implemented")
    }

    override fun getStatus(): String {
        TODO("Not yet implemented")
    }

    override fun getIconThemePath(): Dbusmenu.PropertyIconThemePathType {
        TODO("Not yet implemented")
    }

    override fun GetLayout(parentId: Int, recursionDepth: Int, propertyNames: MutableList<String>?): GetLayoutTuple {
        TODO("Not yet implemented")
    }

    override fun GetGroupProperties(
        ids: MutableList<Int>?,
        propertyNames: MutableList<String>?
    ): MutableList<GetGroupPropertiesStruct> {
        TODO("Not yet implemented")
    }

    override fun GetProperty(id: Int, name: String?): Variant<*> {
        TODO("Not yet implemented")
    }

    override fun Event(id: Int, eventId: String?, data: Variant<*>?, timestamp: UInt32?) {
        TODO("Not yet implemented")
    }

    override fun EventGroup(events: MutableList<EventGroupStruct>?): MutableList<Int> {
        TODO("Not yet implemented")
    }

    override fun AboutToShow(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun AboutToShowGroup(ids: MutableList<Int>?): AboutToShowGroupTuple {
        TODO("Not yet implemented")
    }
}
