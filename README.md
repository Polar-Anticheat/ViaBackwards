# ViaBackwards

[![Latest Release](https://img.shields.io/github/v/release/ViaVersion/ViaBackwards)](https://github.com/ViaVersion/ViaBackwards/releases)
[![Build Status](https://github.com/ViaVersion/ViaBackwards/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/ViaVersion/ViaBackwards/actions)
[![Discord](https://img.shields.io/badge/chat-on%20discord-blue.svg)](https://viaversion.com/discord)

**Allows older Minecraft client versions to connect to newer server versions**

Polar fork
-
This fork aims to temporarily fix the issue of duplicate transaction packets not being handled correctly.
`ids` set in `PingRequests` class does not support duplicates. The fork comments out references to this class.

This is a temporary solution and is not designed to be a permanent fix.

Supported Versions
-
**Green** = ViaVersion\
**Purple** = ViaBackwards addition

![supported_versions](https://i.imgur.com/KpLsMHj.png)

Releases / Dev Builds
-
**Requires [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/) to be installed**
   
You can find the latest dev builds here:\
**Jenkins:** https://ci.viaversion.com/view/ViaBackwards/

**Spigot page:** https://www.spigotmc.org/resources/viabackwards.27448/

**CurseForge page:** https://www.curseforge.com/minecraft/mc-mods/viabackwards

Known issues
-
* Custom 1.17+ min_y and height world values are not and will (most likely) never be supported => Clients older than 1.17 will not be able to see or interact with blocks below y=0 and above y=255
* < 1.17 clients on 1.17+ servers might experience inventory desyncs on certain inventory click actions
* Sound mappings are incomplete ([see here](https://github.com/ViaVersion/ViaBackwards/issues/326))

Other Links
-
**Maven:** https://repo.viaversion.com

**Issue tracker:** https://github.com/ViaVersion/ViaBackwards/issues

**List of contributors:** https://github.com/ViaVersion/ViaBackwards/graphs/contributors

License
-
This project is licensed under the [GNU General Public License](LICENSE).

Special Thanks
-
![https://www.yourkit.com/](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/),
[YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).
