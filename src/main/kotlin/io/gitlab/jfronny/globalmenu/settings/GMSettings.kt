package io.gitlab.jfronny.globalmenu.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

@Service
@State(name = "Global Menu", storages = [Storage("globalmenu.xml")])
class GMSettings : SimplePersistentStateComponent<GMSettings.State>(State()) {
    class State : BaseState() {
        var menu by property(true)
        var decorations by property(true)
    }

    companion object {
        fun getInstance(): GMSettings = ApplicationManager.getApplication().getService(GMSettings::class.java)
    }
}