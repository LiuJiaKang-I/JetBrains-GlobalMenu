package io.gitlab.jfronny.globalmenu.proxy

import com.intellij.openapi.actionSystem.impl.ActionMenu
import com.intellij.openapi.application.EDT
import io.gitlab.jfronny.dbusmenu4j.DMLog
import io.gitlab.jfronny.dbusmenu4j.SwingRootMenu
import io.gitlab.jfronny.globalmenu.GlobalMenu
import io.gitlab.jfronny.globalmenu.reflect.invoke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class ActionRootMenu(bar: JMenuBar, name: String, holder: ActionMenuHolder): SwingRootMenu<ActionRootMenu, ActionMenuHolder>(bar, name, holder, GlobalMenu) {
    override fun runOnEDT(runnable: Runnable) {
        runBlocking {
            launch(Dispatchers.EDT) {
                runnable.invoke()
            }
        }
    }

    fun update(items: List<ActionMenu>) {
        menuItems = items
        syncChildren()
    }

    init {
        syncChildren()
    }

    override fun child(item: JMenuItem, holder: ActionMenuHolder, log: DMLog?) = ActionMenu(item, holder)
}
