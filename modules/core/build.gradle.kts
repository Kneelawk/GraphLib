plugins {
    id("architectury-plugin")
    id("dev.architectury.loom") apply false
}

evaluationDependsOnChildren()

architectury {
    val minecraft_version: String by project
    minecraft = minecraft_version
}

// For some reason other subprojects can't depend directly on this project's subprojects,
// so we need to export our subprojects' configurations from this project.
// This has to be a gradle bug.
configurations {
    create("xplat") {
        isCanBeConsumed = true
        isCanBeResolved = false

        extendsFrom(project(":core:xplat").configurations["namedElements"])
    }
    create("fabric") {
        isCanBeConsumed = true
        isCanBeResolved = false

        extendsFrom(project(":core:fabric").configurations["namedElements"])
        extendsFrom(project(":core:xplat").configurations["namedElements"])
    }
    create("neoforge") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }
}

artifacts {
    // forge doesn't seem to like when mods are split between multiple jars
    add("neoforge", project(":core:neoforge").tasks.named("shadowJar"))
}
