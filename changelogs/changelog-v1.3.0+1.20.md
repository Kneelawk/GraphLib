Changes:

* Fixed loading graph chunks during world-chunk loading causing deadlocks.
* Added `/graphlib <universe-id> rebuildchunks <from-x> <from-y> <from-z> <to-x> <to-y> <to-z>` command for rebuilding
  chunk graph indices if they get corrupted by a server crash.
