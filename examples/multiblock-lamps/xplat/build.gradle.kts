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
    id("com.kneelawk.versioning")
}

evaluationDependsOn(":core-xplat")

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-${parent!!.name}-${project.name}")
}

loom {
//    accessWidenerPath.set(file("src/main/resources/graphlib.accesswidener"))
//    runs {
//        named("client") {
//            ideConfigGenerated(true)
//            programArgs("--width", "1280", "--height", "720")
//        }
//        named("server") {
//            ideConfigGenerated(true)
//        }
//    }
}

architectury {
    val enabled_platforms: String by project
    common(enabled_platforms.split(','))
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }

    mavenLocal()
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val quilt_mappings: String by project
    mappings("org.quiltmc:quilt-mappings:$minecraft_version+build.$quilt_mappings:intermediary-v2")

    // Using modCompileOnly & modLocalRuntime so that these dependencies don't get brought into any projects that depend
    // on this one.

    // Fabric Loader
    val fabric_loader_version: String by project
    modCompileOnly("net.fabricmc:fabric-loader:$fabric_loader_version")
    modLocalRuntime("net.fabricmc:fabric-loader:$fabric_loader_version")

    // GraphLib
    compileOnly(project(":core-xplat", configuration = "namedElements"))
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to project.version))
        }
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
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
        from("LICENSE") {
            rename { "${it}_${archives_base_name}" }
        }
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}
