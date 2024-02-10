/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
    id("com.kneelawk.versioning")
}

evaluationDependsOn(":multiblock-lamps:xplat")

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-${parent!!.name}-${project.name}")
}

base.libsDirectory.set(rootProject.layout.buildDirectory.map { it.dir("libs") })

loom {
    runs {
        named("client") {
            ideConfigGenerated(true)
            programArgs("--width", "1280", "--height", "720")
        }
        named("server") {
            ideConfigGenerated(true)
        }
    }
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

configurations {
    val common = create("common")
    create("shadowCommon")
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentNeoForge").extendsFrom(common)
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://kneelawk.com/maven/") { name = "Kneelawk" }

    mavenLocal()
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val quilt_mappings: String by project
    mappings("org.quiltmc:quilt-mappings:$minecraft_version+build.$quilt_mappings:intermediary-v2")

    val neoforge_version: String by project
    neoForge("net.neoforged:neoforge:$neoforge_version")

    "common"(project(path = ":multiblock-lamps:xplat", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(
        project(
            path = ":multiblock-lamps:xplat",
            configuration = "transformProductionNeoForge"
        )
    ) { isTransitive = false }

    // GraphLib
    compileOnly(project(":core-xplat", configuration = "namedElements"))
    compileOnly(project(":core-neoforge", configuration = "namedElements"))
    runtimeOnly(project(":core-neoforge", configuration = "dev"))
    include(project(":core-neoforge"))

    // GraphLib debug renderer
    compileOnly(project(":debugrender-xplat", configuration = "namedElements"))
    compileOnly(project(":debugrender-neoforge", configuration = "namedElements"))
    runtimeOnly(project(":debugrender-neoforge", configuration = "dev"))
    include(project(":debugrender-neoforge"))

    // KModLib Overlay
    val kml_version: String by project
    modRuntimeOnly("com.kneelawk:kmodlib-overlay-neoforge:$kml_version")
}

tasks {
    processResources {
        from(project(":multiblock-lamps:xplat").sourceSets.main.map { it.resources.asFileTree })

        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier = "dev-shadow"
    }

    remapJar {
        injectAccessWidener = true
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${archives_base_name}" }
        }
    }

    named("sourcesJar", Jar::class) {
        val xplatSources = project(":multiblock-lamps:xplat").tasks.named("sourcesJar", Jar::class)
        dependsOn(xplatSources)
        from(xplatSources.flatMap { task -> task.archiveFile.map { zipTree(it) } })
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}
