plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    setLibsDirectory()
    setRefmaps("transfer-beams")
    applyFabricLoaderDependency()
    applyFabricApiDependency()
    fabricProjectDependency(":core")
    xplatProjectDependency(":debugrender")
    fabricProjectDependency(":syncing-core")
    generateRuns()
}

repositories {
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

dependencies {
    // GraphLib Syncing LNS
    implementation(project(":syncing-lns", configuration = "namedElements"))
    include(project(":syncing-lns"))

    // LibNetworkStack
    val lns_version: String by project
    modImplementation("alexiil.mc.lib:libnetworkstack-base:$lns_version")
    include("alexiil.mc.lib:libnetworkstack-base:$lns_version")
    
    // We actually use KModLib Overlay in order to make nodes visible through blocks
    val kml_version: String by project
    modImplementation("com.kneelawk:kmodlib-overlay-fabric:$kml_version")
    include("com.kneelawk:kmodlib-overlay-fabric:$kml_version")

    // Mod Menu
    val mod_menu_version: String by project
    modLocalRuntime("com.terraformersmc:modmenu:$mod_menu_version")
}

val modId = "transfer_beams"
val genResDir = file("src/main/resources-generated")

loom {
//    accessWidenerPath.set(file("src/main/resources/graphlib.accesswidener"))
    runs {
        create("datagen") {
            inherit(getByName("client"))
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${genResDir}")
            vmArg("-Dfabric-api.datagen.modid=${modId}")

            runDir("build/datagen")
        }
    }
}

sourceSets {
    named("main") {
        resources {
            srcDir(genResDir)
        }
    }
}
