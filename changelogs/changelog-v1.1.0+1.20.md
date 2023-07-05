Changes:

* Added `GraphUniverse.getSidedGraphView(...)` which will not crash when given a `World` that is neither a `ClientWorld`
  nor a `ServerWorld`.
* Deprecated `GraphUniverse.getGraphView(...)` because it will crash when given a `World` that is neither
  a `ClientWorld` nor a `ServerWorld`.
