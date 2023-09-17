Changes:

* Added `link` and `unlink` methods to `Graph` that take whole `Link` objects instead of just the component nodes.
* Added checking code to prevent the creation of duplicate nodes and links.
    * This fixes a bug where duplicate node creation would corrupt one of the graph's internal maps.
    * This also makes sure graph-entities no longer receive duplicate events for duplicate creations.
