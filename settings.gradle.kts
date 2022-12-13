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
        val quilt_mappings_on_loom_version: String by settings
        id("org.quiltmc.quilt-mappings-on-loom") version quilt_mappings_on_loom_version
    }
}

rootProject.name = "graphlib"
