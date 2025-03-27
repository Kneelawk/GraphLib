plugins {
    id("fabric-loom") apply false
    id("com.kneelawk.submodule") apply false
    id("com.kneelawk.mojmap") apply false
    id("com.kneelawk.versioning") apply false
    id("com.kneelawk.kpublish") apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

allprojects {
    // make builds reproducible
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}
