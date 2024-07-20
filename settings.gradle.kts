pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.github.hypfvieh:dbus-java-utils:4.3.2")
    }
}

rootProject.name = "globalmenu"

include("native")