package io.gitlab.jfronny.globalmenu

import com.canonical.Dbusmenu
import com.canonical.appmenu.Registrar
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.impl.IdeFrameImpl
import com.intellij.openapi.wm.impl.ProjectFrameHelper
import io.gitlab.jfronny.globalmenu.proxy.DbusmenuImpl
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.types.UInt32
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
        val conn = DBusConnectionBuilder.forSessionBus().build()

        val menu: Dbusmenu = DbusmenuImpl()
        // firefox seems to use mObjectPath(nsPrintfCString("/com/canonical/menu/%u", sID++))
        conn.exportObject("/com/canonical/dbusmenu", menu)

        if (peer is WLPeer) {
            peer.performLocked {
                val ptr = GlobalMenu.Native.create(peer.nativePtr)
                GlobalMenu.Native.setAddress(ptr, conn.uniqueName, menu.objectPath)
            }
        } else {
            val registrar = conn.getRemoteObject("org.canonical.AppMenu.Registrar", "/com/canonical/AppMenu/Registrar", Registrar::class.java)
            registrar.RegisterWindow(UInt32(peer.nativePtr), DBusPath(menu.objectPath))
        }
    }
}