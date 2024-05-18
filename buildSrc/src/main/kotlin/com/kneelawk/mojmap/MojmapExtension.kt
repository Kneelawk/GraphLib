// Copyright (c) 2022 Emi
// MIT License

package com.kneelawk.mojmap

import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

abstract class MojmapExtension(private val project: Project) {
    val jar by lazy { project.tasks.named("mojmapJar", RemapJarTask::class) }
    val sourcesJar by lazy { project.tasks.named("mojmapSourcesJar", RemapSourcesJarTask::class) }

    fun applyXplatConnection(xplatName: String) {
        val xplat = project.evaluationDependsOn(xplatName)

        val loomEx = project.extensions.getByType(LoomGradleExtensionAPI::class) as LoomGradleExtension

        val mojmapJar = project.tasks.create("mojmapJar", RemapJarTask::class) {
            classpath.from(loomEx.getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY))
            dependsOn(xplat.tasks.named("remapJar"))

            archiveClassifier.set("")

            inputFile.set(xplat.tasks.named("remapJar", RemapJarTask::class).flatMap { it.archiveFile })
            sourceNamespace.set("intermediary")
            targetNamespace.set("named")

            remapperIsolation.set(true)
        }

        val mojmapSourcesJar = project.tasks.create("mojmapSourcesJar", RemapSourcesJarTask::class) {
            classpath.from(loomEx.getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY))
            dependsOn(xplat.tasks.named("remapSourcesJar"))

            archiveClassifier.set("sources")

            inputFile.set(xplat.tasks.named("remapSourcesJar", RemapSourcesJarTask::class).flatMap { it.archiveFile })
            sourceNamespace.set("intermediary")
            targetNamespace.set("named")

            remapperIsolation.set(true)
        }

        project.tasks.named("assemble").configure { dependsOn(mojmapJar, mojmapSourcesJar) }
    }
}
