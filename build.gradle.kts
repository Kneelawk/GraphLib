plugins {
    `maven-publish`
    alias(libs.plugins.quilt.loom)
}

val maven_group: String by project
group = maven_group
val mod_version: String by project
version = mod_version

val archives_base_name: String by project
base {
    archivesName.set(archives_base_name)
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:${libs.versions.quilt.mappings.get()}:v2"))
    })

    // Quilt Loader
    modImplementation(libs.quilt.loader)

    // Quilted Fabric Api
    modImplementation(libs.quilted.fabric.api)

    // We use JUnit 4 because many Minecraft classes require heavy mocking or complete gutting, meaning a custom
    // classloader is required. JUnit 5 does not yet support using custom classloaders.
    testImplementation("junit:junit:4.13.2")
}

tasks {
    processResources {
        inputs.property("version", mod_version)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to mod_version))
        }
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to mod_version))
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
            version = mod_version

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
