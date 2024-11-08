pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.frohnmeyer-wds.de/artifacts") {
            content {
                includeGroup("io.gitlab.jfronny")
                includeGroup("jf.autoversion")
            }
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.github.hypfvieh:dbus-java-utils:5.1.0")
    }
}

rootProject.name = "globalmenu"

include("native")