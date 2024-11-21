package io.gitlab.jfronny.globalmenu

import com.intellij.openapi.diagnostic.Logger
import io.gitlab.jfronny.dbusmenu4j.DMLog

object GlobalMenu : DMLog {
    val debugging = System.getProperty("io.gitlab.jfronny.globalmenu.debug") != null
    val Log: Logger = Logger.getInstance(GlobalMenu::class.java)
    val Native = Native()
//    val Cleaner = java.lang.ref.Cleaner.create()
    override fun warn(message: String?) = Log.warn(message)
    override fun error(text: String?, exception: Throwable?) = Log.error(text, exception)
    override fun isDebug(): Boolean = debugging
}