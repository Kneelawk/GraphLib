# GraphLib v0.1.0+1.18.2

GraphLib version 0.1.0 for Minecraft 1.18.2

This is the initial release version.

## About GraphLib

GraphLib is a rewrite of 2xsaiko's HCTM-Base wirenet functionality for improved speed and memory usage, as well as being
its own stand-alone library. This library is still in an early stage of development, so expect bugs, breaking changes,
etc.

There may still be some breaking changes made to the save format. If/When that happens, a command will be provided that
fixes any errors caused by this.

## Changes from HCTM-Base

There are many changes from the original library, but here are some of the most prominent ones:

* GraphLib is written in Java instead of Kotlin, because that seems to provide the best compatibility with Java
  projects.
* Many classes were renamed. Some examples are:
    * `PartExt` -> `BlockNode`
    * `Network` -> `BlockGraph`
    * `WireNetworkController` -> `BlockGraphController`
* GraphLib stores graphs in separate files so that the ones that are not being used can be unloaded to save memory.
  This idea was taken from 2xsaiko's HCTM-Base in-progress Java-rewrite.
* Graphs-In-Pos information is stored to avoid expensive recalculation. This information is then updated when necessary
  instead of being completely recalculated.
* Graph IDs are incrementing Longs instead of random UUIDs. This allows both for better ID collision avoidance and for
  the use of FastUtil's optimized datastructures.
* `BlockNode`s are decoded by dedicated `BlockNodeDecoder`s registered in a registry instead of by the block associated
  with the `BlockNode`.
