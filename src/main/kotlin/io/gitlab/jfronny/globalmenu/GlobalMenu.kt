package io.gitlab.jfronny.globalmenu

import com.intellij.openapi.diagnostic.Logger

object GlobalMenu {
    val Log: Logger = Logger.getInstance(GlobalMenu::class.java)
    val Native = Native()
    val Cleaner = java.lang.ref.Cleaner.create()
}