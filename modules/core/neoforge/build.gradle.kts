import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

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
    `maven-publish`
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
    id("com.kneelawk.versioning")
}

evaluationDependsOn(":core:xplat")

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-${parent!!.name}-${project.name}")
}

base.libsDirectory.set(rootProject.layout.buildDirectory.map { it.dir("libs") })
java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir("graphlib-${project.name}") })

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

    mavenLocal()
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val quilt_mappings: String by project
    mappings("org.quiltmc:quilt-mappings:$minecraft_version+build.$quilt_mappings:intermediary-v2")

    val neoforge_version: String by project
    neoForge("net.neoforged:neoforge:$neoforge_version")

    "common"(project(path = ":core:xplat", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":core:xplat", configuration = "transformProductionNeoForge")) { isTransitive = false }
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    val shadowJar = named("shadowJar", ShadowJar::class) {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier = "dev-shadow"
    }

    named("remapJar", RemapJarTask::class) {
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

        withJavadocJar()
        withSourcesJar()
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${archives_base_name}" }
        }
    }

    javadoc {
        source(project(":core:xplat").sourceSets.main.get().allJava)
        exclude("com/kneelawk/graphlib/impl")

//        val minecraft_version: String by project
//        val quilt_mappings: String by project
        val jetbrains_annotations_version: String by project
//        val lns_version: String by project
        (options as? StandardJavadocDocletOptions)?.links = listOf(
//            "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/$minecraft_version+build.$quilt_mappings/quilt-mappings-$minecraft_version+build.$quilt_mappings-javadoc.jar/",
            "https://javadoc.io/doc/org.jetbrains/annotations/${jetbrains_annotations_version}/",
//            "https://alexiil.uk/javadoc/libnetworkstack/${lns_version}/"
        )

        options.optionFiles(rootProject.file("javadoc-options.txt"))
    }

    named("sourcesJar", Jar::class) {
        val xplatSources = project(":core:xplat").tasks.named("sourcesJar", Jar::class)
        dependsOn(xplatSources)
        from(xplatSources.flatMap { task -> task.archiveFile.map { zipTree(it) } })
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}

//components.named("java", AdhocComponentWithVariants::class) {
//    withVariantsFromConfiguration(project.configurations["shadowCommon"]) {
//        skip()
//    }
//}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "${parent!!.name}-${project.name}"
            from(components["java"])
        }
    }

    repositories {
        if (System.getenv("PUBLISH_REPO") != null) {
            maven {
                name = "publishRepo"
                url = uri(rootProject.file(System.getenv("PUBLISH_REPO")))
            }
        }
    }
}
