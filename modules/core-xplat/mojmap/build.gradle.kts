// Copyright (c) 2022 Emi
// MIT License

plugins {
    id("com.kneelawk.mojmap")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

mojmap {
    applyXplatConnection(":core-xplat")
}

kpublish {
    createPublication("mojmap", project.parent!!.name, mojmap.jar, mojmap.sourcesJar)
}
