# GraphLib

[![Github Release Status]][Github Release] ![Maven Status]

[Github Release Status]: https://img.shields.io/github/v/release/Kneelawk/GraphLib?include_prereleases&style=flat-square
[Github Release]: https://github.com/Kneelawk/GraphLib/releases/latest
[Maven Status]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fkneelawk.com%2Fmaven%2Fcom%2Fkneelawk%2Fgraphlib%2Fmaven-metadata.xml&style=flat-square

Library for helping mods that use graph networks, like Wired Redstone.

## Depending on GraphLib
GraphLib can be added to a gradle project's dependencies like such:
```groovy
repositories {
    maven {
        url 'https://kneelawk.com/maven/'
        name 'Kneelawk Maven'
    }
}

dependencies {
    modImplementation("com.kneelawk:graphlib:$graphlibVersion")
    include("com.kneelawk:graphlib:$graphlibVersion")
}
```
where `$graphlibVersion` is replaced by GraphLib's latest release version.
