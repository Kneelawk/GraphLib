// Copyright (c) 2022 Emi
// MIT License

plugins {
    id("com.kneelawk.mojmap")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

mojmap {
    applyXplatConnection(":syncing-knet-xplat")
}

kpublish {
    createPublication("mojmap", parent!!.name, mojmap.jar, mojmap.sourcesJar) {
        pom {
            val knet_version: String by rootProject
            withXml {
                val dep = asNode().appendNode("dependencies").appendNode("dependency")
                dep.appendNode("groupId").setValue("com.kneelawk.knet")
                dep.appendNode("artifactId").setValue("xplat-mojmap")
                dep.appendNode("version").setValue(knet_version)
                dep.appendNode("scope").setValue("compile")
            }
        }
    }
}
