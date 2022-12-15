plugins {
    `maven-publish`
    id("fabric-loom")
    id("io.github.juuxel.loom-quiltflower")
    id("org.quiltmc.quilt-mappings-on-loom")
}

val maven_group: String by project
group = maven_group

// System to get the release version if this project is being built as part of a release
val modVersion: String = if (System.getenv("RELEASE_TAG") != null) {
    val releaseTag = System.getenv("RELEASE_TAG")
    val modVersion = releaseTag.substring(1)
    println("Detected Release Version: $modVersion")
    modVersion
} else {
    val mod_version: String by project
    println("Detected Local Version: $mod_version")
    mod_version
}
version = modVersion

val archives_base_name: String by project
base {
    archivesName.set(archives_base_name)
}

loom {
    accessWidenerPath.set(file("src/main/resources/graphlib.accesswidener"))
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val quilt_mappings: String by project
    mappings(loom.layered {
        addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:$minecraft_version+build.$quilt_mappings:v2"))
    })

    // Using modCompileOnly & modLocalRuntime so that these dependencies don't get brought into any projects that depend
    // on this one.

    // Quilt Loader
    val fabric_loader_version: String by project
    modCompileOnly("net.fabricmc:fabric-loader:$fabric_loader_version")
    modLocalRuntime("net.fabricmc:fabric-loader:$fabric_loader_version")

    // Quilted Fabric Api
    val fapi_version: String by project
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fapi_version")
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api:$fapi_version")

    // We use JUnit 4 because many Minecraft classes require heavy mocking or complete gutting, meaning a custom
    // classloader is required. JUnit 5 does not yet support using custom classloaders.
    testImplementation("junit:junit:4.13.2")
}

tasks {
    processResources {
        inputs.property("version", modVersion)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to modVersion))
        }
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to modVersion))
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

    javadoc {
        options {
            optionFiles(file("javadoc-options.txt"))
        }
    }

    test {
        useJUnit()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = maven_group
            artifactId = project.name
            version = modVersion

            from(components["java"])
        }
    }

    repositories {
        if (System.getenv("PUBLISH_REPO") != null) {
            maven {
                name = "publishRepo"
                url = uri(System.getenv("PUBLISH_REPO"))
            }
        }
    }
}
