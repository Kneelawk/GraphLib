Changes:

* Same as version 2.0.0-beta.1.
* Updated to Minecraft 1.21.
* Divided GraphLib up into modules:
    * `core` - Houses the primary GraphLib stuff, like graphs, nodes, and entities.
    * `debugrender` - Houses the debug renderer.
    * `syncing-core` - Houses the client-side graph stuff, allowing syncing implementations to transmit graphs to the
      client.
    * `syncing-knet` - Houses the KNet-based synchronization mechanism.
    * `syncing-lns` - Houses the LibNetworkStack-based synchronization mechanism.
* Made GraphLib multi-platform, supporting both Fabric and NeoForge, supporting both vanilla-gradle and
  architectury-based setups.
* Added Codextra library dependency.
* Moved everything over to using `Codec`s for encoding and decoding.
* Moved syncing over to using `StreamCodec`s for buffer encoding and decoding.
* Removed `LEGACY_UNIVERSE` default graph universe from pre-1.0 versions.
* Moved events system over to using Common Events library.
