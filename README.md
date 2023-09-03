# GraphLib

[![Github Release Status]][Github Release] [![Maven Status]][Maven] [![Javadoc Badge]][Javadoc]

[Github Release Status]: https://img.shields.io/github/v/release/Kneelawk/GraphLib?include_prereleases&style=flat-square

[Github Release]: https://github.com/Kneelawk/GraphLib/releases/latest

[Maven Status]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fkneelawk.com%2Fmaven%2Fcom%2Fkneelawk%2Fgraphlib%2Fmaven-metadata.xml&style=flat-square

[Maven]: https://kneelawk.com/maven#com/kneelawk/graphlib

[Javadoc Badge]: https://img.shields.io/badge/-javadoc-green?style=flat-square

[Javadoc]: https://kneelawk.com/docs#graphlib

Library for helping mods that use graph networks, like Wired Redstone.

## GraphLib and HCTM-Base

This library is based on [HCTM-Base by 2xsaiko][HCTM-Base] and is essentially a rewrite of his block-graph-network code
in Java with some optimizations. Credit goes to 2xsaiko for designing this system in the first place.

[HCTM-Base]: https://github.com/2xsaiko/hctm-base

## Commands

All commands can be accessed via the `/graphlib` command.

### `/graphlib <universe> debugrender start` and `/graphlib <universe> debugrender stop`

These commands are used for starting and stopping the block graph debug renderer. The debug renderer can help you debug
your code that uses GraphLib or figure out why your wires aren't connecting after a server crash.

![2022-12-16_14 56 38](https://user-images.githubusercontent.com/2180089/208538473-8ec33250-22a4-4572-bab9-48748817fd94.png)

### `/graphlib <universe> updateblocks <from> <to>`

This command updates all block-nodes in a given area. If block-nodes were not saved due to a server crash then it may
cause some machines or wires to act strange, not transmitting signals correctly. This command will re-connect those
machines.

### `/graphlib <universe> removeemptygraphs`

Sometimes a server crash can cause a block-graph to get saved without any block-nodes in it. These empty graphs can
clutter up your save file. This command removes these empty graphs.

### `/graphlib <universe> rebuildchunks <from> <to>`

Sometimes a server crash can cause only graphs or only chunk-indexes to be saved but not the other, meaning that the
index used to look up which graphs are in which positions could be out of date. This command rebuilds that index for the
given chunks.

**Note: the command arguments `<from>` and `<to>` are block-positions, not chunk positions. Running this command for one
block in a 16x16x16 chunk section should fix the entire chunk section.**

## Depending on GraphLib

GraphLib can be added to a gradle project's dependencies like such:

```groovy
repositories {
    maven {
        url 'https://kneelawk.com/maven/'
        name 'Kneelawk Maven'
    }
    maven {
        // For LibNetworkStack, which GraphLib depends on
        url 'https://maven.alexiil.uk/'
        name 'AlexIIL Maven'
    }
}

dependencies {
    modImplementation("com.kneelawk:graphlib:$graphlibVersion")
    include("com.kneelawk:graphlib:$graphlibVersion")
}
```

where `$graphlibVersion` is replaced by GraphLib's latest release version.
