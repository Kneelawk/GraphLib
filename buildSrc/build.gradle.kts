plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.9.20"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.architectury.dev/") { name = "Architectury" }
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://kneelawk.com/maven") { name = "Kneelawk" }
}

dependencies {
    val architectury_loom_version: String by project
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:$architectury_loom_version")
}

gradlePlugin {
    plugins {
        create("mojmapPlugin") {
            id = "com.kneelawk.mojmap"
            implementationClass = "com.kneelawk.mojmap.MojmapPlugin"
        }
    }
}
