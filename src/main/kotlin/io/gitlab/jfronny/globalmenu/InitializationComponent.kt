package io.gitlab.jfronny.globalmenu

import com.intellij.ide.AppLifecycleListener

class InitializationComponent : AppLifecycleListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        GlobalMenu.Native.init(getDisplayPtr())
    }
}