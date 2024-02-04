plugins {
    id("architectury-plugin") apply false
    id("dev.architectury.loom") apply false
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
