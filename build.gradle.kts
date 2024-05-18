plugins {
    id("dev.architectury.loom") apply false
    id("com.kneelawk.submodule") apply false
    id("com.kneelawk.mojmap") apply false
    id("com.kneelawk.versioning") apply false
    id("com.kneelawk.kpublish") apply false
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
