package io.gitlab.jfronny.globalmenu

import com.intellij.ide.AppLifecycleListener

class InitializationComponent : AppLifecycleListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        if (GlobalMenu.Native.isSupported) {
            GlobalMenu.Native.init(getDisplayPtr())
        }
    }
}