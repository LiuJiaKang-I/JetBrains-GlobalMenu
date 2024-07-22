package io.gitlab.jfronny.globalmenu.settings

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import io.gitlab.jfronny.globalmenu.GlobalMenu
import javax.swing.JCheckBox
import javax.swing.JPanel

class SettingsComponent {
    val mainPanel: JPanel
    lateinit var menu: Cell<JBCheckBox>
    lateinit var decorations: Cell<JCheckBox>

    init {
        mainPanel = panel {
            if (!GlobalMenu.Native.isSupported) {
                row {
                    label("Could not load native library: ${GlobalMenu.Native.problem.orElseThrow()}").apply {
                        bold()
                        component.foreground = JBColor.RED
                    }
                    label("The plugin will not work on Wayland")
                }
            } else {
                row {
                    label("Native library loaded successfully").apply {
                        component.foreground = JBColor.GREEN
                    }
                    label("The plugin should work as expected")
                }
            }
            row {
                menu = checkBox("Enable global menu").apply {
                    check(GMSettings.getInstance().state.menu)
                }
            }
            row {
                decorations = checkBox("Enable server-side decorations").apply {
                    check(GMSettings.getInstance().state.decorations)
                }
            }
        }
    }
}