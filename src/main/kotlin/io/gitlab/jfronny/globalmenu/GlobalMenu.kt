package io.gitlab.jfronny.globalmenu

import com.intellij.openapi.diagnostic.Logger

object GlobalMenu {
    val debugging = System.getProperty("io.gitlab.jfronny.globalmenu.debug") != null
    val Log: Logger = Logger.getInstance(GlobalMenu::class.java)
    val Native = Native()
}