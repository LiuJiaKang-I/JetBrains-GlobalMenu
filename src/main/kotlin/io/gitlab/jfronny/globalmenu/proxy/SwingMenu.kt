package io.gitlab.jfronny.globalmenu.proxy

import com.intellij.openapi.actionSystem.impl.ActionMenu
import com.intellij.openapi.actionSystem.impl.ActionMenuItem
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import io.gitlab.jfronny.dbusmenu4j.Menu
import io.gitlab.jfronny.globalmenu.GlobalMenu
import io.gitlab.jfronny.globalmenu.buildArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.output.ByteArrayOutputStream
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.lang.reflect.Modifier
import javax.imageio.ImageIO
import javax.swing.*

class SwingMenu(private val menuItem: JMenuItem?, id: Int, private val holder: SwingMenuHolder) : Menu.Abstract() {
    private val id_ = id;
    override fun getId() = id_
    constructor(menuItem: JMenuItem, holder: SwingMenuHolder) : this(menuItem, holder.getId(menuItem), holder)
    constructor(id: Int, holder: SwingMenuHolder) : this(null, id, holder)

    override fun isSeparator() = menuItem == null
    override fun getLabel() = menuItem?.text ?: ""
    override fun isEnabled() = menuItem?.isEnabled ?: false
    override fun isVisible() = menuItem?.isVisible ?: false
    private val iconData_: ByteArray? by lazy { menuItem?.icon?.let { icon ->
        // This is somewhat expensive, but we need to do it
        // At least avoid doing it multiple times
        val width = icon.iconWidth
        val height = icon.iconHeight

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        bufferedImage.createGraphics().apply {
            icon.paintIcon(menuItem, this, 0, 0)
            dispose()
        }

        try {
            ByteArrayOutputStream().use { stream ->
                ImageIO.write(bufferedImage, "png", stream)
                stream.toByteArray()
            }
        } catch (e: Exception) {
            GlobalMenu.Log.error("Failed to convert icon to byte array", e)
            null
        }
    } }
    override fun getIconData() = iconData_

    override fun getShortcut() = menuItem?.accelerator?.let { ks ->
        buildArray {
            val modifiers = ks.modifiers
            if (modifiers and InputEvent.SHIFT_DOWN_MASK != 0) accept("Shift")
            if (modifiers and InputEvent.CTRL_DOWN_MASK != 0) accept("Ctrl")
            if (modifiers and InputEvent.META_DOWN_MASK != 0) accept("Meta")
            if (modifiers and InputEvent.ALT_DOWN_MASK != 0) accept("Alt")
            if (modifiers and InputEvent.ALT_GRAPH_DOWN_MASK != 0) accept("AltGraph")
            if (modifiers and InputEvent.BUTTON1_DOWN_MASK != 0) accept("Button1")
            if (modifiers and InputEvent.BUTTON2_DOWN_MASK != 0) accept("Button2")
            if (modifiers and InputEvent.BUTTON3_DOWN_MASK != 0) accept("Button3")
            accept(keyEvents[ks.keyCode] ?: "UNKNOWN")
        }
    }

    override fun getToggleType() = when (menuItem) {
        is ActionMenuItem -> {
            //TODO handle action items
            if (menuItem.isToggleable) "checkmark"
            else null
        }
        is JRadioButtonMenuItem -> "radio"
        is JCheckBoxMenuItem -> "checkmark"
        else -> null
    }
    override fun getToggleState() = if (toggleType?.isNotEmpty() == true) if (menuItem!!.isSelected) 1 else 0 else -1
    private var _children: List<Menu>? = null
    override fun getChildren() = _children

    override fun onEvent() {
        if (menuItem == null) return
        ApplicationManager.getApplication().invokeLater(menuItem::doClick)
        when (menuItem) {
            is ActionMenuItem -> {}
            is ActionMenu -> {}
            is JCheckBoxMenuItem -> menuItem.isSelected = !menuItem.isSelected
            is JRadioButtonMenuItem -> menuItem.isSelected = true
        }
    }

    override fun update() {
        super.update()
        try {
            if (menuItem is ActionMenu) {
                runBlocking {
                    launch(Dispatchers.EDT) {
                        menuItem.removeAll()
                        menuItem.isSelected = true
                        menuItem.fillMenu()
                        syncChildren(2)
                    }
                }
            }
        } catch (e: Exception) {
            GlobalMenu.Log.error("Failed to update menu", e)
            throw e
        }
    }

    fun syncChildren(deepness: Int) {
//        GlobalMenu.Log.warn("Syncing children for $label")
        _children = when (menuItem) {
            is ActionMenu -> {
                val ch = mutableListOf<Menu>()
                for (each in menuItem.popupMenu.components) {
                    if (each == null) continue
                    if (each is JSeparator) {
                        ch.add(SwingMenu(System.identityHashCode(each), holder))
                        continue
                    }
                    if (each !is JMenuItem) {
                        continue
                    }
                    val cmi = SwingMenu(each, holder)
                    if (deepness > 1) {
                        if (each is ActionMenu) {
                            each.removeAll()
                            each.isSelected = true
                            each.fillMenu() // This is REALLY expensive and on the EDT TODO: find out whether we can avoid it
                        }
                        cmi.syncChildren(deepness - 1)
                    }
                    ch.add(cmi)
                }
                ch
            }
            is JMenu -> (0 until menuItem.itemCount).map { SwingMenu(menuItem.getItem(it), holder) }
            else -> null
        }
    }

    override fun toString(): String {
        return "SwingMenu(id=$id_, menuItem=$menuItem)"
    }

    companion object {
        val keyEvents: Map<Int, String> = KeyEvent::class.java.fields
            .filter { it.modifiers == Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL && it.name.startsWith("VK_") }
            .associate { it.getInt(null) to it.name.substring(3) }
    }
}