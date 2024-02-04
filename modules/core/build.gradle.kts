plugins {
    id("architectury-plugin")
    id("dev.architectury.loom") apply false
}

architectury {
    val minecraft_version: String by project
    minecraft = minecraft_version
}
