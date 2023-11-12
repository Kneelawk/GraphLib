plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("versioningPlugin") {
            id = "com.kneelawk.versioning"
            implementationClass = "com.kneelawk.versioning.VersioningPlugin"
        }
        create("submodulePlugin") {
            id = "com.kneelawk.submodule"
            implementationClass = "com.kneelawk.submodule.SubmodulePlugin"
        }
    }
}
