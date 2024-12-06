pluginManagement {
    repositories {
        maven("https://maven.quiltmc.org/repository/release") {
            name = "Quilt"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.architectury.dev/") {
            name = "Architectury"
        }
        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
        }
        maven("https://maven.kneelawk.com/releases/") {
            name = "Kneelawk"
        }
        gradlePluginPortal()
    }
    plugins {
        val loom_version: String by settings
        id("fabric-loom") version loom_version
        val moddev_version: String by settings
        id("net.neoforged.moddev") version moddev_version
        val remapcheck_version: String by settings
        id("com.kneelawk.remapcheck") version remapcheck_version
        val versioning_version: String by settings
        id("com.kneelawk.versioning") version versioning_version
        val kpublish_version: String by settings
        id("com.kneelawk.kpublish") version kpublish_version
        val submodule_version: String by settings
        id("com.kneelawk.submodule") version submodule_version
    }
}

rootProject.name = "graphlib"

fun module(enabled: Boolean, name: String) {
    if (!enabled) return
    include(name)
    project(":$name").projectDir = File(rootDir, "modules/${name.replace(':', '/')}")
}

fun module(name: String, vararg submodules: Pair<Boolean, String>) {
    include(name)
    project(":$name").projectDir = File(rootDir, "modules/$name")

    for ((enabled, submodule) in submodules) {
        if (!enabled) continue
        include("$name:$submodule")
        project(":$name:$submodule").projectDir = File(rootDir, "modules/$name/${submodule.replace(':', '/')}")
    }
}

fun example(enabled: Boolean, name: String) {
    if (!enabled) return
    include(name)
    project(":$name").projectDir = File(rootDir, "examples/${name.replace(':', '/')}")
}

fun example(name: String, vararg submodules: Pair<Boolean, String>) {
    include(name)
    project(":$name").projectDir = File(rootDir, "examples/$name")

    for ((enabled, submodule) in submodules) {
        if (!enabled) continue
        include("$name:$submodule")
        project(":$name:$submodule").projectDir = File(rootDir, "examples/$name/${submodule.replace(':', '/')}")
    }
}

fun javadoc(enabled: Boolean, name: String) {
    if (!enabled) return
    include("javadoc-$name")
    project(":javadoc-$name").projectDir = File(rootDir, "javadoc/$name")
}

val xplat = true
val mojmap = true
val fabric = true
val neoforge = true

module(xplat, "core-xplat")
module(mojmap, "core-xplat-mojmap")
module(fabric, "core-fabric")
module(neoforge, "core-neoforge")
module(xplat, "debugrender-xplat")
module(mojmap, "debugrender-xplat-mojmap")
module(fabric, "debugrender-fabric")
module(neoforge, "debugrender-neoforge")
module(xplat, "syncing-core-xplat")
module(mojmap, "syncing-core-xplat-mojmap")
module(fabric, "syncing-core-fabric")
module(neoforge, "syncing-core-neoforge")
module(xplat, "syncing-knet-xplat")
module(mojmap, "syncing-knet-xplat-mojmap")
module(fabric, "syncing-knet-fabric")
module(neoforge, "syncing-knet-neoforge")
module(fabric, "syncing-lns")

example("multiblock-lamps", xplat to "xplat", fabric to "fabric", neoforge to "neoforge")

javadoc(xplat, "xplat")
javadoc(fabric, "fabric")
javadoc(neoforge, "neoforge")

include(":remapCheck")
