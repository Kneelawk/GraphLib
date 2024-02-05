plugins {
    id("architectury-plugin")
    id("dev.architectury.loom") apply false
}

architectury {
    val minecraft_version: String by project
    minecraft = minecraft_version
}

tasks.create("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

allprojects {
    // make builds reproducible
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}
