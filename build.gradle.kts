import io.gitlab.jfronny.scripts.changelogHtml
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.7.0"
    id("jf.autoversion")
}

group = "io.gitlab.jfronny"

repositories {
    mavenCentral()
    maven("https://maven.frohnmeyer-wds.de/artifacts")

    intellijPlatform {
        defaultRepositories()
        snapshots()
    }
}

val extraResources by configurations.creating

dependencies {
    intellijPlatform {
        intellijIdeaUltimate("2025.2.1") // https://plugins.jetbrains.com/docs/intellij/intellij-artifacts.html
    }
    extraResources(project(path = ":native", configuration = "results"))
    implementation("io.gitlab.jfronny:commons:1.8.0-SNAPSHOT")
    implementation("io.gitlab.jfronny:commons-unsafe:1.8.0-SNAPSHOT")
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
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_21
    }

    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("252.*")
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
        jvmArgs(
            "-Dawt.toolkit.name=WLToolkit",
            "-Dio.gitlab.jfronny.globalmenu.debug"
        )
    }
}
