package io.gitlab.jfronny.globalmenu.proxy

interface MenuHolder {
    fun find(menuId: Int): Menu?
}