plugins {
    id("architectury-plugin")
    id("dev.architectury.loom") apply false
}

architectury {
    val minecraft_version: String by project
    minecraft = minecraft_version
}

/*
plugins {
    id("fabric-loom")
    id("com.kneelawk.versioning")
}

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-${project.name}")
}

base.libsDirectory.set(rootProject.layout.buildDirectory.map { it.dir("libs") })
java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir("graphlib-${project.name}") })

loom {
//    accessWidenerPath.set(file("src/main/resources/graphlib.accesswidener"))
    runs {
        named("client") {
            ideConfigGenerated(true)
            programArgs("--width", "1280", "--height", "720")
        }
        named("server") {
            ideConfigGenerated(true)
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.alexiil.uk/") { name = "AlexIIL" }
    maven("https://kneelawk.com/maven/") { name = "Kneelawk" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }

    mavenLocal()
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val quilt_mappings: String by project
    mappings("org.quiltmc:quilt-mappings:$minecraft_version+build.$quilt_mappings:intermediary-v2")

    // Using modCompileOnly & modLocalRuntime so that these dependencies don't get brought into any projects that depend
    // on this one.

    // Fabric Loader
    val fabric_loader_version: String by project
    modCompileOnly("net.fabricmc:fabric-loader:$fabric_loader_version")
    modLocalRuntime("net.fabricmc:fabric-loader:$fabric_loader_version")

    // Fabric Api
    val fapi_version: String by project
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fapi_version")
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api:$fapi_version")

    // GraphLib
    compileOnly(project(":core:xplat", configuration = "namedElements"))
    implementation(project(":core:fabric", configuration = "namedElements"))
    include(project(":core:fabric", configuration = "namedElements"))
    // We need the debug-renderer at runtime
    runtimeOnly(project(":debugrender", configuration = "namedElements"))

    // LibNetworkStack
    val lns_version: String by project
    modLocalRuntime("alexiil.mc.lib:libnetworkstack-base:$lns_version")

    // KModLib Overlay
    val kml_version: String by project
    modLocalRuntime("com.kneelawk:kmodlib-overlay:$kml_version")

    // Mod Menu
    val mod_menu_version: String by project
    modLocalRuntime("com.terraformersmc:modmenu:$mod_menu_version")

    // We use JUnit 4 because many Minecraft classes require heavy mocking or complete gutting, meaning a custom
    // classloader is required. JUnit 5 does not yet support using custom classloaders.
    testImplementation("junit:junit:4.13.2")
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to project.version))
        }
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${archives_base_name}" }
        }
    }

    test {
        useJUnit()
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}
 */
