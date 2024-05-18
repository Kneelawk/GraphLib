// Copyright (c) 2022 Emi
// MIT License

package com.kneelawk.mojmap

import com.kneelawk.getProperty
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

class MojmapPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("dev.architectury.loom")

        val baseEx = project.extensions.getByType(BasePluginExtension::class)
        val loomEx = project.extensions.getByType(LoomGradleExtensionAPI::class)

        project.extensions.create("mojmap", MojmapExtension::class.java, project)

        val mavenGroup = project.getProperty<String>("maven_group")
        project.group = mavenGroup
        val archivesBaseName = project.getProperty<String>("archives_base_name")
        baseEx.archivesName.set("${archivesBaseName}-${project.parent!!.name}-${project.name}")

        project.dependencies {
            val minecraftVersion = project.getProperty<String>("minecraft_version")
            add("minecraft", "com.mojang:minecraft:$minecraftVersion")

            add("mappings", loomEx.officialMojangMappings())
        }

        project.tasks.apply {
            named("remapJar", AbstractArchiveTask::class).configure {
                archiveClassifier.set("remapJar-disabled")
            }
            named("remapSourcesJar", AbstractArchiveTask::class).configure {
                archiveClassifier.set("remapSourcesJar-disabled")
            }
        }
    }
}
