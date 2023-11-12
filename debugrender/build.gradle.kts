plugins {
    `maven-publish`
    id("fabric-loom")
    id("io.github.juuxel.loom-quiltflower")
    id("com.kneelawk.versioning")
}

val suffix = "debugrender"

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-$suffix")
}

project.layout.buildDirectory.set(rootProject.layout.buildDirectory.map { it.dir(suffix) })
base.libsDirectory.set(rootProject.layout.buildDirectory.map { it.dir("libs") })
java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir("graphlib-$suffix") })

loom {
//    accessWidenerPath.set(file("src/main/resources/graphlib.accesswidener"))
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.alexiil.uk/") { name = "AlexIIL" }
    maven("https://kneelawk.com/maven/") { name = "Kneelawk" }

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

    // GraphLib Core
    implementation(project(":core", configuration = "namedElements"))

    // KModLib Overlay
    val kml_version: String by project
    modImplementation("com.kneelawk:kmodlib-overlay:$kml_version")
    include("com.kneelawk:kmodlib-overlay:$kml_version")

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

        withJavadocJar()
        withSourcesJar()
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${archives_base_name}" }
        }
    }

    javadoc {
        exclude("com/kneelawk/graphlib/debugrender/impl")

        val minecraft_version: String by project
        val quilt_mappings: String by project
        val jetbrains_annotations_version: String by project
        val lns_version: String by project
        (options as? StandardJavadocDocletOptions)?.links = listOf(
//            "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/$minecraft_version+build.$quilt_mappings/quilt-mappings-$minecraft_version+build.$quilt_mappings-javadoc.jar/",
            "https://javadoc.io/doc/org.jetbrains/annotations/${jetbrains_annotations_version}/",
//            "https://alexiil.uk/javadoc/libnetworkstack/${lns_version}/"
        )

        options.optionFiles(rootProject.file("javadoc-options.txt"))
    }

    test {
        useJUnit()
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithQuiltflower"))
        }
    }

    // make builds reproducible
    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = maven_group
            artifactId = "${rootProject.name}-$suffix"
            version = project.version.toString()

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
