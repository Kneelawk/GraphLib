plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    setLibsDirectory()
    setRefmaps("graphlib-syncing-lns")
    applyFabricLoaderDependency()
    applyFabricApiDependency()
    fabricProjectDependency(":core")
    fabricProjectDependency(":syncing-core")
    setupJavadoc()
}

dependencies {
    // LibNetworkStack
    val lns_version: String by project
    modApi("alexiil.mc.lib:libnetworkstack-base:$lns_version")
    include("alexiil.mc.lib:libnetworkstack-base:$lns_version")
}

kpublish {
    createPublication()
}
