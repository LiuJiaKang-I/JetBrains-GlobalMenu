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
        classpath("com.github.hypfvieh:dbus-java-utils:5.0.0")
    }
}

rootProject.name = "globalmenu"

include("native")