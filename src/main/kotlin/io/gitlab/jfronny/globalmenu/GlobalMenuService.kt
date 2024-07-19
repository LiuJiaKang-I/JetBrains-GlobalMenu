package io.gitlab.jfronny.globalmenu

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.impl.IdeFrameImpl
import com.intellij.openapi.wm.impl.ProjectFrameHelper
import javax.swing.JMenuBar

class GlobalMenuService(private val app: Application) : ApplicationActivationListener {
    override fun applicationActivated(ideFrame: IdeFrame) {
        super.applicationActivated(ideFrame)
        if (!GlobalMenu.Native.isSupported) return
        ideFrame.project?.let { project ->
            println(ideFrame.javaClass)
            if (ideFrame is ProjectFrameHelper) {
//                ideFrame.frame.jMenuBar = JMenuBar()
//                ideFrame.rootPane.jMenuBar
                visualize(ideFrame.rootPane.jMenuBar, ideFrame.rootPane.peer)
            } else if (ideFrame is IdeFrameImpl) {
                visualize(ideFrame.jMenuBar, ideFrame.peer)
            }
            GlobalMenu.Log.warn("Activated: ${project.name}")
        }
    }

    fun visualize(menu: JMenuBar, peer: Peer) {
        GlobalMenu.Log.warn("Using peer: $peer")
        //TODO send to compositor
        for (i in 0 until menu.menuCount) {
            val submenu = menu.getMenu(i)
            for (j in 0 until submenu.itemCount) {
                val component = submenu.getItem(j)
                GlobalMenu.Log.warn("${submenu.text}.${component?.text}")
            }
        }
        peer.performLocked {
            val ptr = GlobalMenu.Native.create(peer.nativePtr)
//            peer.registerCleaner {
//                GlobalMenu.Native.destroy(ptr)
//            }
        }
    }
}