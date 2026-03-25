package dev.jfronny.globalmenu

import com.intellij.ide.AppLifecycleListener
import dev.jfronny.globalmenu.reflect.getDisplayPtr

class InitializationComponent : AppLifecycleListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        if (GlobalMenu.Native.isSupported) {
            GlobalMenu.Native.init(getDisplayPtr())
        }
    }
}