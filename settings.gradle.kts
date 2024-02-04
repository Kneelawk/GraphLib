pluginManagement {
    repositories {
        maven("https://maven.quiltmc.org/repository/release") {
            name = "Quilt"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven ("https://maven.architectury.dev/") {
            name = "Architectury"
        }
        maven ("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
        }
        gradlePluginPortal()
    }
    plugins {
        val loom_version: String by settings
        id("fabric-loom") version loom_version
        val architectury_version: String by settings
        id("architectury-plugin") version architectury_version
        val architectury_loom_version: String by settings
        id("dev.architectury.loom") version architectury_loom_version
        val shadow_version: String by settings
        id("com.github.johnrengelman.shadow") version shadow_version
    }
}

rootProject.name = "graphlib"

fun module(name: String) {
    include(name)
    project(":$name").projectDir = File(rootDir, "modules/${name.replace(':', '/')}")
}

fun example(name: String) {
    include(name)
    project(":$name").projectDir = File(rootDir, "examples/${name.replace(':', '/')}")
}

module("core")
module("core:xplat")
module("core:fabric")
module("core:neoforge")
module("debugrender")
module("syncing")
example("multiblock-lamps")
