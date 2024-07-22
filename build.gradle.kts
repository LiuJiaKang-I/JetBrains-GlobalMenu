import com.jetbrains.plugin.structure.base.utils.createParentDirs
import org.freedesktop.dbus.utils.generator.InterfaceCodeGenerator
import org.jetbrains.intellij.platform.gradle.utils.asPath
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively

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
    extraResources(project(mapOf("path" to ":native", "configuration" to "results")))
    implementation("io.gitlab.jfronny:commons-unsafe:2.0.0-SNAPSHOT")
//    implementation("com.github.hypfvieh:dbus-java-core:5.0.0")
//    implementation("com.github.hypfvieh:dbus-java-transport-native-unixsocket:5.0.0")
}

val copyExtraResources by tasks.creating(Copy::class) {
    from(extraResources)
    into(layout.buildDirectory.dir("extraResources"))
}

sourceSets {
    main {
        resources {
            srcDir(copyExtraResources)
        }
        java {
            srcDir(layout.buildDirectory.dir("generated/dbus/menu"))
            srcDir(layout.buildDirectory.dir("generated/dbus/registrar"))
        }
    }
}

abstract class InterfaceGenerateTask : DefaultTask() {
    @get:InputFile abstract val inputFile: RegularFileProperty
    @get:Input abstract val objectPath: Property<String>
    @get:Input abstract val busName: Property<String>
    @get:OutputDirectory abstract val outputFile: DirectoryProperty
    @TaskAction fun generate() {
        val input = inputFile.get().asPath
        val introspectionData = Files.readString(input)
        val output = outputFile.get().asPath
        val generator = InterfaceCodeGenerator(
            false,
            introspectionData,
            objectPath.get(),
            busName.get()
        )
        val analyze = generator.analyze(true)!!
        if (analyze.isEmpty()) throw IllegalStateException("No interfaces found")
        @OptIn(ExperimentalPathApi::class)
        output.deleteRecursively()
        output.createDirectories()
        val illegalStruct = Regex("List<org\\.freedesktop\\.dbus\\.Struct<Integer>, ([^_\\n]+)>")
        val illegalTuple = Regex("public ([A-Za-z]+Tuple) ")
        val fieldPattern = Regex("@Position\\(\\d+\\)\\r?\\n +private (.+) [a-zA-Z]+;")
        val structMemory = LinkedHashMap<String, String>()
        val tupleMemory = LinkedHashMap<String, String>()
        for (entry in analyze) {
            if (entry.key.path.equals("/.java") || entry.key.path.endsWith("Tuple.java")) continue // Skip incorrectly generated file
            val pth = output.resolve(entry.key.path.trimStart('/'))
            pth.createParentDirs()
            // Fix the incorrect generic type
            Files.writeString(pth, entry.value.replace(illegalStruct) { match ->
                structMemory.computeIfAbsent(match.groups[1]!!.value) { type ->
                    val name = "Struct${structMemory.size + 1}"
                    Files.writeString(output.resolve("com/canonical").resolve("$name.java"), """
                        package com.canonical;
                        
                        import org.freedesktop.dbus.Struct;
                        import org.freedesktop.dbus.annotations.Position;
                        import org.freedesktop.dbus.types.Variant;
                        
                        import java.util.List;
                        import java.util.Map;
                        
                        public class $name extends Struct {
                            @Position(0) public final int a;
                            @Position(1) public final $type b;
                            
                            public $name(int a, $type b) {
                                this.a = a;
                                this.b = b;
                            }
                        }
                    """.trimIndent())
                    name
                }
            }.replace(illegalTuple) { match ->
                tupleMemory.computeIfAbsent(match.groups[1]!!.value) { type ->
                    val impl = analyze[analyze.keys.first { it.path.contains(type) }]!!
                    val found = fieldPattern.findAll(impl).toList()
                    if (found.size != 2) throw IllegalStateException("Tuple must have exactly two fields")
                    "io.gitlab.jfronny.globalmenu.DPair<${found[0].groups[1]!!.value}, ${found[1].groups[1]!!.value}>"
                }
            })
        }
    }
}

tasks {
    val generateDbus by registering(InterfaceGenerateTask::class) {
        group = "custom"
        objectPath = "/"
        busName = ""
        inputFile = file("src/main/protocols/dbus-menu.xml")
        outputFile = layout.buildDirectory.dir("generated/dbus/menu")
    }
    val generateDbusRegistrar by registering(InterfaceGenerateTask::class) {
        group = "custom"
        objectPath = "/"
        busName = ""
        inputFile = file("src/main/protocols/com.canonical.AppMenu.Registrar.xml")
        outputFile = layout.buildDirectory.dir("generated/dbus/registrar")
    }
    compileJava {
        dependsOn(generateDbus, generateDbusRegistrar)
    }
    compileKotlin {
        dependsOn(generateDbus, generateDbusRegistrar)
    }

    buildSearchableOptions {
        jvmArgs("-Dio.gitlab.jfronny.globalmenu.disable")
    }

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
