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

package com.kneelawk.submodule

import com.kneelawk.getProperty
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

abstract class SubmoduleExtension(private val project: Project) {
    lateinit var xplatName: String
    val transitiveProjectDependencies = mutableListOf<String>()

    fun setLibsDirectory() {
        val baseEx = project.extensions.getByType(BasePluginExtension::class)
        baseEx.libsDirectory.set(project.rootProject.layout.buildDirectory.dir("libs"))
    }

    fun setRefmaps(basename: String) {
        val refmapName = "${basename}.refmap.json"

        val loomEx = project.extensions.getByType(LoomGradleExtensionAPI::class)
        loomEx.mixin.defaultRefmapName.set(refmapName)

        project.tasks.named("processResources", ProcessResources::class).configure {
            filesMatching("*.mixins.json") {
                expand(mapOf("refmap" to refmapName))
            }

            inputs.property("refmap", refmapName)
        }
    }

    fun applyNeoforgeDependency() {
        project.dependencies.apply {
            val neoforgeVersion = project.getProperty<String>("neoforge_version")
            add("neoForge", "net.neoforged:neoforge:$neoforgeVersion")
        }
    }

    fun applyFabricLoaderDependency() {
        project.dependencies.apply {
            val fabricLoaderVersion = project.getProperty<String>("fabric_loader_version")
            add("modCompileOnly", "net.fabricmc:fabric-loader:$fabricLoaderVersion")
            add("modLocalRuntime", "net.fabricmc:fabric-loader:$fabricLoaderVersion")
        }
    }

    fun applyFabricApiDependency() {
        project.dependencies.apply {
            val fapiVersion = project.getProperty<String>("fapi_version")
            add("modCompileOnly", "net.fabricmc.fabric-api:fabric-api:$fapiVersion")
            add("modLocalRuntime", "net.fabricmc.fabric-api:fabric-api:$fapiVersion")
        }
    }

    fun applyXplatConnection(xplatName: String) {
        this.xplatName = xplatName

        val xplatProject = project.evaluationDependsOn(xplatName)

        val loomEx = project.extensions.getByType(LoomGradleExtensionAPI::class)
        val xplatLoom = xplatProject.extensions.getByType(LoomGradleExtensionAPI::class)
        val xplatSubmodule = xplatProject.extensions.getByType(SubmoduleExtension::class)
        val xplatSourceSets = xplatProject.extensions.getByType(SourceSetContainer::class)
        val mainSource = xplatSourceSets.named("main")

        if (loomEx.mods.findByName("main") != null) {
            loomEx.mods.named("main").configure { sourceSet(mainSource.get()) }
        } else {
            loomEx.mods.create("main") {
                sourceSet(project.extensions.getByType(SourceSetContainer::class).named("main").get())
                sourceSet(mainSource.get())
            }
        }

        val onNeoForge = project.findProperty("loom.platform") == "neoforge"

        if (!onNeoForge) {
            loomEx.mixin.defaultRefmapName.set(xplatLoom.mixin.defaultRefmapName)
        }

        project.dependencies.apply {
            add("compileOnly", project(xplatName, configuration = "namedElements"))
        }

        for (transitiveDep in xplatSubmodule.transitiveProjectDependencies) {
            if (onNeoForge) {
                neoforgeProjectDependency(transitiveDep)
            } else {
                fabricProjectDependency(transitiveDep)
            }
        }

        project.tasks.apply {
            named("processResources", ProcessResources::class.java).configure {
                from(mainSource.map { it.resources })

                if (onNeoForge) {
                    exclude("fabric.mod.json")

                    filesMatching("*.mixins.json") {
                        filter { if (it.contains("refmap")) "" else it }
                    }
                } else {
                    val refmapName = loomEx.mixin.defaultRefmapName.get()

                    filesMatching("*.mixins.json") {
                        expand(mapOf("refmap" to refmapName))
                    }

                    inputs.property("refmap", refmapName)
                }
            }

            withType<JavaCompile>().configureEach {
                source(xplatSourceSets.named("main").map { it.allJava })
            }

            named("sourcesJar", Jar::class.java).configure {
                from(mainSource.map { it.allSource })
            }

            named("javadoc", Javadoc::class.java).configure {
                source(mainSource.map { it.allJava })
            }
        }
    }

    fun generateRuns() {
        val loomEx = project.extensions.getByType(LoomGradleExtensionAPI::class);
        loomEx.runs {
            named("client") {
                ideConfigGenerated(true)
                programArgs("--width", "1280", "--height", "720")
            }
            named("server") {
                ideConfigGenerated(true)
            }
        }
    }

    fun createDevExport() {
        project.configurations.apply {
            create("dev") {
                isCanBeConsumed = true
                isCanBeResolved = false
            }
        }

        val jarExt = project.tasks.run {
            create("jarExt", Jar::class.java) {
                from(named("compileJava"))
                from(named("processResources"))
                from(project.rootProject.file("LICENSE")) {
                    rename { "${it}_${project.rootProject.name}" }
                }
                archiveClassifier.set("jarExt")
                destinationDirectory.set(project.layout.buildDirectory.dir("devlibs"))
            }
        }

        project.artifacts.add("dev", jarExt)

        project.tasks.named("assemble").configure { dependsOn(jarExt) }
    }

    fun xplatProjectDependency(projectBase: String, transitive: Boolean = true) {
        project.dependencies.apply {
            add("compileOnly", project("${projectBase}-xplat", configuration = "namedElements"))
            add("testCompileOnly", project("${projectBase}-xplat", configuration = "namedElements"))
        }

        if (transitive) {
            transitiveProjectDependencies.add(projectBase)
        }
    }

    fun fabricProjectDependency(projectBase: String) {
        project.dependencies.apply {
            add("compileOnly", project("${projectBase}-xplat", configuration = "namedElements"))
            add("implementation", project("${projectBase}-fabric", configuration = "namedElements"))
            add("include", project("${projectBase}-fabric"))
            add("testCompileOnly", project("${projectBase}-xplat", configuration = "namedElements"))
            add("testImplementation", project("${projectBase}-fabric", configuration = "namedElements"))
        }
    }

    fun neoforgeProjectDependency(projectBase: String) {
        project.dependencies.apply {
            add("compileOnly", project("${projectBase}-xplat", configuration = "namedElements"))
            add("compileOnly", project("${projectBase}-neoforge", configuration = "namedElements"))
            add("runtimeOnly", project("${projectBase}-neoforge", configuration = "dev"))
            add("include", project("${projectBase}-neoforge"))
            add("testCompileOnly", project("${projectBase}-xplat", configuration = "namedElements"))
            add("testCompileOnly", project("${projectBase}-neoforge", configuration = "namedElements"))
            add("testRuntimeOnly", project("${projectBase}-neoforge", configuration = "dev"))
        }
    }
}
