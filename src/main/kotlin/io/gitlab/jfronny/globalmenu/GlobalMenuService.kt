package io.gitlab.jfronny.globalmenu

import com.canonical.appmenu.Registrar
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.impl.IdeFrameImpl
import com.intellij.openapi.wm.impl.ProjectFrameHelper
import io.gitlab.jfronny.globalmenu.proxy.DbusmenuImpl
import io.gitlab.jfronny.globalmenu.proxy.SwingMenuHolder
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
                visualize(ideFrame.rootPane.jMenuBar, ideFrame.rootPane.peer)
            } else if (ideFrame is IdeFrameImpl) {
                visualize(ideFrame.jMenuBar, ideFrame.peer)
            }
            GlobalMenu.Log.warn("Activated: ${project.name}")
        }
    }

    fun visualize(menu: JMenuBar, peer: Peer) {
        val conn = DBusConnectionBuilder.forSessionBus().build()

        val windowPtr = peer.nativePtr
        val menu = DbusmenuImpl(windowPtr, SwingMenuHolder(menu, "DBusMenuRoot"))
        conn.unExportObject(menu.objectPath)
        conn.exportObject(menu)

        if (peer is WLPeer) {
            peer.performLocked {
                val ptr = GlobalMenu.Native.create(windowPtr)
                GlobalMenu.Native.setAddress(ptr, conn.uniqueName, menu.objectPath)
            }
        } else {
            val registrar = conn.getRemoteObject("org.canonical.AppMenu.Registrar", "/com/canonical/AppMenu/Registrar", Registrar::class.java)
            registrar.RegisterWindow(UInt32(windowPtr), DBusPath(menu.objectPath))
        }
    }
}