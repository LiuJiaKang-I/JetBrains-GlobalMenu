import dev.jfronny.autoversion.changelogHtml
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    kotlin("jvm") version "2.3.20" // https://www.jetbrains.com/legal/third-party-software/?product=iiu
    id("org.jetbrains.intellij.platform") version "2.13.1"
    id("dev.jfronny.autoversion")
}

group = "io.gitlab.jfronny"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
        snapshots()
    }
}

val extraResources by configurations.creating

dependencies {
    intellijPlatform {
        intellijIdea("2026.1") // https://plugins.jetbrains.com/docs/intellij/intellij-artifacts.html
    }
    extraResources(project(path = ":native", configuration = "results"))
    implementation("dev.jfronny.commons:commons:2.0.0")
    implementation("dev.jfronny.commons:commons-unsafe:2.0.0")
    implementation(project(":dbusmenu4j"))
}

val copyExtraResources by tasks.registering(Copy::class) {
    from(extraResources)
    into(layout.buildDirectory.dir("extraResources"))
}

sourceSets {
    main {
        resources {
            srcDir(copyExtraResources)
        }
    }
}

tasks {

    buildSearchableOptions {
        jvmArgs(
            "-Dio.gitlab.jfronny.globalmenu.disable"
        )
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_25
    }

    patchPluginXml {
        sinceBuild.set("261")
        untilBuild.set("261.*")
        changeNotes = changelogHtml
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    runIde {
        environment("WAYLAND_DEBUG", "1")
        environment("_JAVA_OPTIONS", "")
        jvmArgs(
            "-Dawt.toolkit.name=WLToolkit",
            "-Dio.gitlab.jfronny.globalmenu.debug",
            "-Dsun.awt.wl.WindowDecorationStyle=builtin",
            "-Djava.util.prefs.userRoot=${layout.buildDirectory.dir("userPrefs").get().asFile.absolutePath}"
        )
    }
}
