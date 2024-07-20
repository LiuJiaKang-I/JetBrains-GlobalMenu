package io.gitlab.jfronny.globalmenu.proxy

import io.gitlab.jfronny.globalmenu.GlobalMenu
import org.apache.commons.io.output.ByteArrayOutputStream
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.lang.reflect.Modifier
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JRadioButtonMenuItem

class SwingMenu(private val menuItem: JMenuItem?, private val holder: SwingMenuHolder) : Menu {
    override val id = holder.getId(menuItem)
    override val isSeparator: Boolean get() = menuItem == null
    override val label: String get() = menuItem?.text ?: ""
    override val isEnabled: Boolean get() = menuItem?.isEnabled ?: false
    override val isVisible: Boolean get() = menuItem?.isVisible ?: false
    override val iconData: ByteArray? get() = menuItem?.icon?.let { icon ->
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
    }

    override val shortcut: Array<String>? get() = menuItem?.accelerator?.let { ks ->
        val s = getModifiersText(ks.modifiers)
        val vk = keyEvents[ks.keyCode] ?: "UNKNOWN"
        val l = mutableListOf<String>()
        val st = StringTokenizer(s)
        while (st.hasMoreTokens()) l.add(st.nextToken())
        l.add(vk)
        l.toTypedArray()
    }

    private fun getModifiersText(modifiers: Int): String = buildString {
        if (modifiers and InputEvent.SHIFT_DOWN_MASK != 0) append("Shift ")
        if (modifiers and InputEvent.CTRL_DOWN_MASK != 0) append("Ctrl ")
        if (modifiers and InputEvent.META_DOWN_MASK != 0) append("Meta ")
        if (modifiers and InputEvent.ALT_DOWN_MASK != 0) append("Alt ")
        if (modifiers and InputEvent.ALT_GRAPH_DOWN_MASK != 0) append("AltGraph ")
        if (modifiers and InputEvent.BUTTON1_DOWN_MASK != 0) append("Button1 ")
        if (modifiers and InputEvent.BUTTON2_DOWN_MASK != 0) append("Button2 ")
        if (modifiers and InputEvent.BUTTON3_DOWN_MASK != 0) append("Button3 ")
    }

    override val toggleType: String? get() = when (menuItem) {
        is JRadioButtonMenuItem -> "radio"
        is JCheckBoxMenuItem -> "checkmark"
        else -> null
    }
    override val toggleState: Int get() = if (toggleType?.isNotEmpty() == true) if (menuItem!!.isSelected) 1 else 0 else -1
    override val children: List<Menu>? get() = if (menuItem is JMenu) {
        (0 until menuItem.itemCount).map { SwingMenu(menuItem.getItem(it), holder) }
    } else null

    override fun onEvent() {
        val event = ActionEvent(menuItem, ActionEvent.ACTION_PERFORMED, menuItem!!.actionCommand)
        for (it in menuItem.actionListeners) it.actionPerformed(event)
        if (menuItem is JCheckBoxMenuItem) menuItem.isSelected = !menuItem.isSelected
        if (menuItem is JRadioButtonMenuItem) menuItem.isSelected = true
    }

    companion object {
        val keyEvents: Map<Int, String> = KeyEvent::class.java.fields
            .filter { it.modifiers == Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL && it.name.startsWith("VK_") }
            .associate { it.getInt(null) to it.name.substring(3) }
    }
}