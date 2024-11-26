pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.frohnmeyer-wds.de/artifacts") {
            content {
                includeGroup("io.gitlab.jfronny")
                includeGroup("jf.autoversion")
                includeGroup("jf.maven-publish")
            }
        }
    }
    plugins {
        id("jf.autoversion") version "1.6-SNAPSHOT"
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
include("dbusmenu4j")