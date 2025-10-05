# Vivecraft Spigot Extensions
VSE is a companion plugin for [Vivecraft](http://www.vivecraft.org), the VR mod for Java Minecraft.
VSE is for [Spigot](https://www.spigotmc.org/), [Paper](https://papermc.io/software/paper) and [Folia](https://papermc.io/software/folia) servers and adds several enhancements for VR players.

# Features
- Vivecraft players will see other Vivecraft players head, arm and leg movements.
- Support for Vivecraft 2-handed archery.
- Assign permission groups for VR players.
- Fixes projectiles and dropped items from VR players.
- Shrinks Creeper explosion radius for VR players from 3 to 1.75m (Configurable)
- Reduces Mobs melee attack radius for VR players ny -0.4m (Configurable)
- Support for Vivecraft Roomscale Blocking (Toggleable)
- Support for Vivecraft Dual Wielding (Toggleable)
- Support for Vivecraft Climbey Tools (Toggleable)
- Support for Vivecraft Arrow Headshots (Configurable)
- Support for faster block breaking (Toggleable)
- Option to limit server to Vivecraft players only.

See the config.yml for all available configuration options.

# Installation
Download from the [Releases](https://github.com/Vivecraft/Vivecraft-Spigot-Extension/releases) page. Just use the latest release, they are backwards compatible, unless stated otherwise.

Install as you would any other Spigot/Bukkit plugin by placing the jar in the /plugins folder.

# Developer Information
## Building the plugin from source
Building the plugin is a bit scuffed right now, and relies on a few gradle tasks to run in specific order:

- run `gradlew generateAccessors` (takenaka -> generateAccessors)
- run `gradlew rebuildStubs` (generate -> rebuildStubs)
- if you are using an IDE reload gradle now, the previous task gerated some subprojects
- run `gradlew build` (build -> build)

## Metadata
VSE provides Spigot metadata on `Player` objects so other plugins can provide special support for handed interactions or somesuch. If you aren’t sure what metadata is, check the [Spigot documentation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/metadata/Metadatable.html). The API supports multiple plugins using the same metadata key, so make sure you filter to our specific plugin name (`Vivecraft-Spigot-Extensions`).

Every player has a head and two hands (obviously), each of which have a 6DOF position and rotation. There are also some tertiary values so you can determine how to properly handle a particular player. The full set of available keys is as follows:

Key(s) | Value
--- | -----
`head.pos`, `righthand.pos`, `lefthand.pos` | `Location` representing the absolute position in the world of the VR object. Also includes the direction for convenience.
`head.dir`, `righthand.dir`, `lefthand.dir` | `Vector` representing the forward direction of the VR object. This is gimbal locked; if you want up or right vectors, use the `rot` value below.
`head.rot`, `righthand.rot`, `lefthand.rot` | Array of 4 floats, representing a quaternion with the order `w,x,y,z`. You’ll need a `Quaternion` class to deal with this properly, but it’s much more flexible than the `dir` value. As of 1.19.4 Minecraft ships with Jom, whch has one of those, and this plugin also ships it for older versions.
`seated` | `Boolean` representing the player is in seated mode. This mode disables hand tracking and places the VR hands to the sides of the head, to allow for keyboard and mouse play in VR.
`height` | `Float` representing whether the player's calibrated height, which mainly affects how tall they appear to other players.
`activehand` | `String` representing which hand (left or right) last performed some actions. Currently throwing projectiles such as snowballs.

## API
We also have an API to access more data, like the history of all available body parts, for more details on that see the [wiki](https://github.com/Vivecraft/Vivecraft-Spigot-Extension/wiki/API).
