package dev.jfronny.globalmenu.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class SettingsConfigurable: Configurable {
    private var component: SettingsComponent? = null
    override fun getDisplayName(): String = "Global Menu"
    override fun getPreferredFocusedComponent(): JComponent? = component?.menu?.component

    override fun createComponent(): JComponent {
        component = SettingsComponent()
        return component!!.mainPanel
    }

    override fun isModified(): Boolean {
        val state = GMSettings.getInstance().state
        return component?.menu?.component?.isSelected != state.menu ||
                component?.decorations?.component?.isSelected != state.decorations
    }

    override fun apply() {
        val state = GMSettings.getInstance().state
        state.menu = component?.menu?.component?.isSelected ?: true
        state.decorations = component?.decorations?.component?.isSelected ?: true
    }

    override fun reset() {
        val state = GMSettings.getInstance().state
        component?.menu?.component?.isSelected = state.menu
        component?.decorations?.component?.isSelected = state.decorations
    }

    override fun disposeUIResources() {
        component = null
    }
}