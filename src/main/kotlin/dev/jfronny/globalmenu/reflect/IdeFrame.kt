package dev.jfronny.globalmenu.reflect

import com.intellij.openapi.wm.IdeFrame
import dev.jfronny.commons.unsafe.reflect.Reflect
import dev.jfronny.dbusmenu4j.Peer
import javax.swing.JFrame
import javax.swing.JMenuBar

private val pfhClass = Class.forName("com.intellij.openapi.wm.impl.ProjectFrameHelper")
private val ifiClass = Class.forName("com.intellij.openapi.wm.impl.IdeFrameImpl")
private val getFrame = Reflect.instanceFunction(pfhClass, "getFrame", ifiClass).unchecked

/**
 * @see com.intellij.openapi.wm.impl.ProjectFrameHelper.frame
 */

fun IdeFrame.introspect(): IdeFrameIntrospection? {
    if (this is JFrame) return IdeFrameIntrospection(Peer.Resolver.resolve(this), this.jMenuBar, this)
    if (pfhClass.isInstance(this)) {
        val frame = getFrame(this) as JFrame
        val root = frame.rootPane
        return IdeFrameIntrospection(Peer.Resolver.resolve(root), root.jMenuBar, frame)
    }
    return null
}

class IdeFrameIntrospection(val peer: Peer, val jMenuBar: JMenuBar?, val frame: JFrame)