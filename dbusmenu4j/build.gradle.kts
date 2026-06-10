import com.jetbrains.plugin.structure.base.utils.createParentDirs
import org.freedesktop.dbus.utils.generator.InterfaceCodeGenerator
import org.jetbrains.intellij.platform.gradle.utils.asPath
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively

plugins {
    `java-library`
    id("jf.maven-publish")
}

group = "io.gitlab.jfronny"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    api("com.github.hypfvieh:dbus-java-core:5.1.0")
    implementation("com.github.hypfvieh:dbus-java-transport-native-unixsocket:5.1.0")
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
            busName.get(),
            null,
            true
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
                    "dev.jfronny.dbusmenu4j.DPair<${found[0].groups[1]!!.value}, ${found[1].groups[1]!!.value}>"
                }
            })
        }
    }
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/dbus/menu"))
            srcDir(layout.buildDirectory.dir("generated/dbus/registrar"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
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
}