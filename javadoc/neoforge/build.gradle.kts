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
}

evaluationDependsOn(":core-xplat")
evaluationDependsOn(":core-neoforge")
evaluationDependsOn(":debugrender-xplat")
evaluationDependsOn(":debugrender-neoforge")
evaluationDependsOn(":syncing-core-xplat")
evaluationDependsOn(":syncing-core-neoforge")

java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir("neoforge") })

architectury {
    neoForge()
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.alexiil.uk/") { name = "AlexIIL" }
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
    
    // modules
    compileOnly(project(":core-xplat", configuration = "namedElements"))
    compileOnly(project(":core-neoforge", configuration = "namedElements"))
    compileOnly(project(":debugrender-xplat", configuration = "namedElements"))
    compileOnly(project(":debugrender-neoforge", configuration = "namedElements"))
    compileOnly(project(":syncing-core-xplat", configuration = "namedElements"))
    compileOnly(project(":syncing-core-neoforge", configuration = "namedElements"))

    // KModLib Overlay
    val kml_version: String by project
    modCompileOnly("com.kneelawk:kmodlib-overlay-neoforge:$kml_version")
}

tasks.javadoc {
    source(project(":core-xplat").sourceSets.main.get().allJava)
    source(project(":core-neoforge").sourceSets.main.get().allJava)
    source(project(":debugrender-xplat").sourceSets.main.get().allJava)
    source(project(":debugrender-neoforge").sourceSets.main.get().allJava)
    source(project(":syncing-core-xplat").sourceSets.main.get().allJava)
    source(project(":syncing-core-neoforge").sourceSets.main.get().allJava)

    exclude("com/kneelawk/graphlib/impl")
    exclude("com/kneelawk/graphlib/neoforge/impl")
    exclude("com/kneelawk/graphlib/debugrender/impl")
    exclude("com/kneelawk/graphlib/debugrender/neoforge/impl")
    exclude("com/kneelawk/graphlib/syncing/impl")
    exclude("com/kneelawk/graphlib/syncing/neoforge/impl")

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
