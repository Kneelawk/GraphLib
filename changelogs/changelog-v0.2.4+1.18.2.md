# GraphLib v0.2.4+1.18.2

GraphLib version 0.2.4 for Minecraft 1.18.2

Changes:

* Fixes issue where `/graphlib removeemptygraphs` wouldn't detect graphs with letters `A`-`F` in their filenames.
    * Yes, I forgot to set the radix when parsing graph filenames. That's fixed now.
