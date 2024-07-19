plugins {
    java
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.intellij.platform") version "2.0.0-beta9"
}

group = "io.gitlab.jfronny"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.frohnmeyer-wds.de/artifacts")

    intellijPlatform {
        defaultRepositories()
    }
}

val extraResources by configurations.creating

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("242.20224.91")
        instrumentationTools()
    }
    implementation("io.gitlab.jfronny:commons-unsafe:2.0.0-SNAPSHOT")
    extraResources(project(mapOf("path" to ":native", "configuration" to "results")))
}

val copyExtraResources by tasks.creating(Copy::class) {
    from(extraResources)
    into(layout.buildDirectory.dir("extraResources"))
}

sourceSets {
    main {
        resources {
            this.srcDir(copyExtraResources)
        }
    }
}

abstract class RunToolTask : AbstractExecTask<RunToolTask>(RunToolTask::class.java) {
    @get:InputFile abstract val inputFile: RegularFileProperty
    @get:OutputFile abstract val outputFile: RegularFileProperty
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("243.*")
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
        this.jvmArgs("-Dawt.toolkit.name=WLToolkit")
    }
}
