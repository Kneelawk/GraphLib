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
        maven("https://kneelawk.com/maven") {
            name = "Kneelawk"
        }
        gradlePluginPortal()
    }
    plugins {
        val architectury_loom_version: String by settings
        id("dev.architectury.loom") version architectury_loom_version
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

fun module(name: String) {
    include(name)
    project(":$name").projectDir = File(rootDir, "modules/${name.replace(':', '/')}")
}

fun module(name: String, vararg submodules: String) {
    include(name)
    project(":$name").projectDir = File(rootDir, "modules/$name")

    for (submodule in submodules) {
        include("$name:$submodule")
        project(":$name:$submodule").projectDir = File(rootDir, "modules/$name/${submodule.replace(':', '/')}")
    }
}

fun example(name: String) {
    include(name)
    project(":$name").projectDir = File(rootDir, "examples/${name.replace(':', '/')}")
}

fun example(name: String, vararg submodules: String) {
    include(name)
    project(":$name").projectDir = File(rootDir, "examples/$name")

    for (submodule in submodules) {
        include("$name:$submodule")
        project(":$name:$submodule").projectDir = File(rootDir, "examples/$name/${submodule.replace(':', '/')}")
    }
}

fun javadoc(name: String) {
    include("javadoc-$name")
    project(":javadoc-$name").projectDir = File(rootDir, "javadoc/$name")
}

module("core-xplat")
module("core-xplat-mojmap")
module("core-fabric")
module("core-neoforge")
module("debugrender-xplat")
module("debugrender-xplat-mojmap")
module("debugrender-fabric")
module("debugrender-neoforge")
module("syncing-core-xplat")
module("syncing-core-xplat-mojmap")
module("syncing-core-fabric")
module("syncing-core-neoforge")
module("syncing-knet-xplat")
module("syncing-knet-xplat-mojmap")
module("syncing-knet-fabric")
module("syncing-knet-neoforge")
module("syncing-lns")

example("multiblock-lamps", "xplat", "fabric", "neoforge")

javadoc("xplat")
javadoc("fabric")
javadoc("neoforge")

include(":remapCheck")
