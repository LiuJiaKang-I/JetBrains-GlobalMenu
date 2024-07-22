package io.gitlab.jfronny.globalmenu

import com.canonical.appmenu.Registrar
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.impl.IdeFrameImpl
import com.intellij.openapi.wm.impl.ProjectFrameHelper
import com.intellij.platform.ide.menu.IdeJMenuBar
import io.gitlab.jfronny.globalmenu.proxy.DbusmenuImpl
import io.gitlab.jfronny.globalmenu.proxy.SwingMenuHolder
import io.gitlab.jfronny.globalmenu.settings.GMSettings
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.types.UInt32
import javax.swing.JMenuBar

class GlobalMenuService(private val app: Application) : ApplicationActivationListener {
    override fun applicationActivated(ideFrame: IdeFrame) {
        super.applicationActivated(ideFrame)
        if (!GlobalMenu.Native.isSupported) return
        if (!GMSettings.getInstance().state.menu) return
        if (ideFrame is ProjectFrameHelper) {
            visualize(ideFrame.rootPane.jMenuBar, ideFrame.rootPane.peer)
        } else if (ideFrame is IdeFrameImpl) {
            visualize(ideFrame.jMenuBar, ideFrame.peer)
        }
    }

    override fun applicationDeactivated(ideFrame: IdeFrame) {
        lastMenu?.let { Disposer.dispose(it) }
    }

    private var lastMenu: Disposable? = null
    private var connection: DBusConnection? = null

    fun visualize(menu: JMenuBar, peer: Peer) = app.invokeLater {
        lastMenu?.let { Disposer.dispose(it) }
        lastMenu = Disposer.newDisposable()

        val conn = connection ?: DBusConnectionBuilder.forSessionBus().build()
        connection = conn

        val menuHolder = SwingMenuHolder(menu, "DBusMenuRoot")
        if (menu is IdeJMenuBar) {
            menu.addUpdateGlobalMenuRootsListener {
                menuHolder.update(menu.rootMenuItems)
            }
//            menu.updateMenuActions(true)
        }
//        IdeEventQueue.getInstance().addDispatcher({ e ->
//            if (e !is KeyEvent) false
//            else if (!e.isAltDown) false
//            else {
//                val src = e.component
//                val wndParent = if (src is Window) src else SwingUtilities.windowForComponent(src)
//                val eventChar = e.keyChar.uppercaseChar()
//
//
//                true
//            }
//        }, lastMenu!!)

        val windowPtr = peer.nativePtr
        val menu = DbusmenuImpl(windowPtr, menuHolder)
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