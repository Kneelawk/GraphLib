pluginManagement {
    repositories {
        maven("https://maven.quiltmc.org/repository/release") {
            name = "Quilt"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
    plugins {
        val loom_version: String by settings
        id("fabric-loom") version loom_version
        val loom_quiltflower_version: String by settings
        id("io.github.juuxel.loom-quiltflower") version loom_quiltflower_version
    }
}

rootProject.name = "graphlib"

fun module(name: String) {
    include(name)
    project(":$name").projectDir = File(rootDir, "modules/$name")
}

fun example(name: String) {
    include(name)
    project(":$name").projectDir = File(rootDir, "examples/$name")
}

module("core")
module("debugrender")
module("syncing")
example("multiblock-lamps")
example("transfer-beams")
