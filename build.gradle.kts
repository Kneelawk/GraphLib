tasks.create("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
