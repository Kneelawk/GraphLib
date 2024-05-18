/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
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

package com.kneelawk.versioning

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.kneelawk.getProperty

class VersioningPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val ext = target.extensions

        val releaseTag = System.getenv("RELEASE_TAG")
        val modVersion = if (releaseTag != null) {
            val modVersion = releaseTag.substring(1)
            println("Detected Release Version: $modVersion")
            modVersion
        } else {
            val modVersion = target.getProperty<String>("mod_version")
            println("Detected Local Version: $modVersion")
            modVersion
        }

        if (modVersion.isEmpty()) {
            throw IllegalStateException("Failed to detect version")
        }

        ext.extraProperties.set("modVersion", modVersion)
        target.version = modVersion
    }
}
