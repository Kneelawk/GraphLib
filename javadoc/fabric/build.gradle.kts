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
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
}

evaluationDependsOn(":core-xplat")
evaluationDependsOn(":core-fabric")
evaluationDependsOn(":debugrender-xplat")
evaluationDependsOn(":debugrender-fabric")
evaluationDependsOn(":syncing-core-xplat")
evaluationDependsOn(":syncing-core-fabric")
evaluationDependsOn(":syncing-lns")

submodule {
    applyFabricLoaderDependency()
    applyFabricApiDependency()
    setupJavadoc()
}

java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir("fabric") })

dependencies {
    // modules
    compileOnly(project(":core-xplat", configuration = "namedElements"))
    compileOnly(project(":core-fabric", configuration = "namedElements"))
    compileOnly(project(":debugrender-xplat", configuration = "namedElements"))
    compileOnly(project(":debugrender-fabric", configuration = "namedElements"))
    compileOnly(project(":syncing-core-xplat", configuration = "namedElements"))
    compileOnly(project(":syncing-core-fabric", configuration = "namedElements"))
    compileOnly(project(":syncing-knet-xplat", configuration = "namedElements"))
    compileOnly(project(":syncing-knet-fabric", configuration = "namedElements"))
    compileOnly(project(":syncing-lns", configuration = "namedElements"))

    // KModLib Overlay
    val kml_version: String by project
    modCompileOnly("com.kneelawk:kmodlib-overlay-fabric:$kml_version")

    // LibNetworkStack
    val lns_version: String by project
    modCompileOnly("alexiil.mc.lib:libnetworkstack-base:$lns_version")
}

tasks.javadoc {
    source(project(":core-xplat").sourceSets.main.get().allJava)
    source(project(":core-fabric").sourceSets.main.get().allJava)
    source(project(":debugrender-xplat").sourceSets.main.get().allJava)
    source(project(":debugrender-fabric").sourceSets.main.get().allJava)
    source(project(":syncing-core-xplat").sourceSets.main.get().allJava)
    source(project(":syncing-core-fabric").sourceSets.main.get().allJava)
    source(project(":syncing-knet-xplat").sourceSets.main.get().allJava)
    source(project(":syncing-knet-fabric").sourceSets.main.get().allJava)
    source(project(":syncing-lns").sourceSets.main.get().allJava)
}
