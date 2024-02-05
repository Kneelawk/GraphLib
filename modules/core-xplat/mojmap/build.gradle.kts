import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask

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

base.libsDirectory.set(rootProject.layout.buildDirectory.map { it.dir("libs") })
java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir("graphlib-${parent!!.name}-${project.name}") })

dependencies {
    val minecraft_version: String by rootProject
    minecraft("com.mojang:minecraft:$minecraft_version")

    mappings(loom.officialMojangMappings())
}

val mojmapJar = tasks.create("mojmapJar", RemapJarTask::class) {
    classpath.from((loom as LoomGradleExtension).getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY))
    dependsOn(project(":core-xplat").tasks.remapJar)

    inputFile.set(project(":core-xplat").tasks.remapJar.flatMap { it.archiveFile })
    sourceNamespace.set("intermediary")
    targetNamespace.set("named")

    remapperIsolation.set(true)
}

val mojmapSourcesJar = tasks.create("mojmapSourcesJar", RemapSourcesJarTask::class) {
    classpath.from((loom as LoomGradleExtension).getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY))
    dependsOn(project(":core-xplat").tasks.remapSourcesJar)

    archiveClassifier.set("sources")

    inputFile.set(project(":core-xplat").tasks.remapSourcesJar.flatMap { it.archiveFile })
    sourceNamespace.set("intermediary")
    targetNamespace.set("named")

    remapperIsolation.set(true)
}

tasks.build.configure {
    dependsOn(mojmapJar)
    dependsOn(mojmapSourcesJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "${parent!!.name}-${project.name}"
            artifact(mojmapJar) {
                builtBy(mojmapJar)
                classifier = ""
            }
            artifact(mojmapSourcesJar) {
                builtBy(mojmapSourcesJar)
                classifier = "sources"
            }
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
