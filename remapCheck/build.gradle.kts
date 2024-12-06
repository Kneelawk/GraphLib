plugins { 
    id("fabric-loom")
    id("com.kneelawk.remapcheck")
}

remapCheck {
    val minecraft_version: String by project
    val yarn_version: String by project
    applyTargetMapping("net.fabricmc:yarn:$minecraft_version+build.$yarn_version:v2")
    checkRemap {
        targetProject(":core-fabric")
        taskNameBase("core")
    }
    checkRemap {
        targetProject(":debugrender-fabric")
        taskNameBase("debugrender")
    }
    checkRemap {
        targetProject(":syncing-core-fabric")
        taskNameBase("syncing-core")
    }
    checkRemap {
        targetProject(":syncing-knet-fabric")
        taskNameBase("syncing-knet")
    }
    checkRemap {
        targetProject(":syncing-lns")
        taskNameBase("syncing-lns")
    }
}
