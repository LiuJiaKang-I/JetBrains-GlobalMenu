import org.gradle.internal.jvm.Jvm

plugins {
    `cpp-library`
}

group = rootProject.group
version = rootProject.version

abstract class RunToolTask : AbstractExecTask<RunToolTask>(RunToolTask::class.java) {
    @get:InputFile abstract val inputFile: RegularFileProperty
    @get:OutputFile abstract val outputFile: RegularFileProperty
}

library {
    binaries.configureEach {
        val compileTask = compileTask.get()
        val dir = "${Jvm.current().javaHome}/include"
        compileTask.includes.from(dir)

        val osFamily = targetPlatform.targetMachine.operatingSystemFamily
        if (osFamily.isMacOs) compileTask.includes.from("$dir/darwin")
        else if (osFamily.isLinux) compileTask.includes.from("$dir/linux")
        else if (osFamily.isWindows) compileTask.includes.from("$dir/win32")

        compileTask.source.from(fileTree(mapOf("dir" to "src/main/c", "include" to "**/*.c")))
        compileTask.source.from(fileTree(mapOf("dir" to layout.buildDirectory.dir("generated/wayland"), "include" to "**/*.c")))
        compileTask.includes.from(layout.buildDirectory.dir("generated/wayland"))

        if (toolChain is VisualCpp) {
            compileTask.compilerArgs.addAll("/TC")
        } else if (toolChain is GccCompatibleToolChain) {
            compileTask.compilerArgs.addAll("-x", "c", "-std=c11")
        }
    }
}

val results by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

afterEvaluate {
//    val jar by tasks.creating(Jar::class) {
//        destinationDirectory = layout.buildDirectory
//        archiveClassifier.set("native")
//        dependsOn()
//        from()
//    }
    artifacts {
        add("results", layout.buildDirectory.file("lib/main/release/libnative.so")) {
            builtBy(tasks.named("assembleRelease"))
        }
    }
}

tasks {
    val generateAppmenuHeader by registering(RunToolTask::class) {
        group = "custom"
        inputFile = file("src/main/protocols/appmenu.xml")
        outputFile = layout.buildDirectory.file("generated/wayland/appmenu.h")
        commandLine("wayland-scanner", "client-header", inputFile.asFile.get().absolutePath, outputFile.asFile.get().absolutePath)
    }
    val generateAppmenuGlue by registering(RunToolTask::class) {
        group = "custom"
        inputFile = file("src/main/protocols/appmenu.xml")
        outputFile = layout.buildDirectory.file("generated/wayland/appmenu.c")
        commandLine("wayland-scanner", "private-code", inputFile.asFile.get().absolutePath, outputFile.asFile.get().absolutePath)
    }
    // shell glue is needed for a symbol used in xdg-decoration-unstable-v1
    val generateShellGlue by registering(RunToolTask::class) {
        group = "custom"
        inputFile = file("src/main/protocols/xdg-shell.xml")
        outputFile = layout.buildDirectory.file("generated/wayland/xdg-shell.c")
        commandLine("wayland-scanner", "private-code", inputFile.asFile.get().absolutePath, outputFile.asFile.get().absolutePath)
    }
    val generateDecorationHeader by registering(RunToolTask::class) {
        group = "custom"
        inputFile = file("src/main/protocols/xdg-decoration-unstable-v1.xml")
        outputFile = layout.buildDirectory.file("generated/wayland/xdg-decoration-unstable-v1.h")
        commandLine("wayland-scanner", "client-header", inputFile.asFile.get().absolutePath, outputFile.asFile.get().absolutePath)
    }
    val generateDecorationGlue by registering(RunToolTask::class) {
        group = "custom"
        inputFile = file("src/main/protocols/xdg-decoration-unstable-v1.xml")
        outputFile = layout.buildDirectory.file("generated/wayland/xdg-decoration-unstable-v1.c")
        commandLine("wayland-scanner", "private-code", inputFile.asFile.get().absolutePath, outputFile.asFile.get().absolutePath)
    }
    afterEvaluate {
        named("compileDebugCpp") {
            dependsOn(generateAppmenuHeader, generateAppmenuGlue, generateShellGlue, generateDecorationHeader, generateDecorationGlue)
        }
        named("compileReleaseCpp") {
            dependsOn(generateAppmenuHeader, generateAppmenuGlue, generateShellGlue, generateDecorationHeader, generateDecorationGlue)
        }
    }
}