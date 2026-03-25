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
        id("dev.jfronny.autoversion") version "1.2"
        id("jf.maven-publish") version "1.8-SNAPSHOT"
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