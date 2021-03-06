# GraphLib

[![Github Release Status]][Github Release] ![Maven Status]

[Github Release Status]: https://img.shields.io/github/v/release/Kneelawk/GraphLib?include_prereleases&style=flat-square

[Github Release]: https://github.com/Kneelawk/GraphLib/releases/latest

[Maven Status]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fkneelawk.com%2Fmaven%2Fcom%2Fkneelawk%2Fgraphlib%2Fmaven-metadata.xml&style=flat-square

Library for helping mods that use graph networks, like Wired Redstone.

## GraphLib and HCTM-Base

This library is based on [HCTM-Base by 2xsaiko][HCTM-Base] and is essentially a rewrite of his block-graph-network code
in Java with some optimizations. Credit goes to 2xsaiko for designing this system in the first place.

[HCTM-Base]: https://github.com/2xsaiko/hctm-base

## Maintenance Commands

All maintenance commands can be accessed via the `\graphlib` command.

### `\graphlib updateblocks <from> <to>`

This command updates all block-nodes in a given area. If block-nodes were not saved due to a server crash then it may
cause some machines or wires to act strange, not transmitting signals correctly. This command will re-connect those
machines.

### `\graphlib removeemptygraphs`

Sometimes a server crash can cause a block-graph to get saved without any block-nodes in it. These empty graphs can
clutter up your save file. This command removes these empty graphs.

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
