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

See the config.yml when you run the plugin, or the [Config](https://github.com/Vivecraft/Vivecraft-Spigot-Extension/wiki/Config) wiki entry for all available configuration options.

# Installation
Download from [Modrinth](https://modrinth.com/project/vivecraft-spigot-extension) or the [Releases](https://github.com/Vivecraft/Vivecraft-Spigot-Extension/releases) page. Just use the latest release, they are backwards compatible, unless stated otherwise.

Install as you would any other Spigot/Bukkit plugin by placing the jar in the /plugins folder.

# Versioning
The plugins version number is split into two parts, the first part is the Vivecraft version that the plugin supports the features of, and the second part is the release version, this increments with fixes or when adding support for new Minecraft versions.  
Example:  
`1.3.3-0`: This version supports the features of Vivecraft `1.3.3` and is the first release for that version

# Using Proxies
When using proxy servers you might need to add a compatibility plugin to your proxy server so that our data is correctly forwarded to the spigot/paper/folia server.  
A this moment we have extensions for
- BungeeCoord: [Vivecraft BungeeCord Extensions](https://github.com/Techjar/Vivecraft_BungeeCord_Extensions/releases)
- Velocity: [Vivecraft Velocity Extensions](https://github.com/Techjar/Vivecraft_Velocity_Extensions/releases)

# Developer Information
## Building the plugin from source
Building the plugin is a bit scuffed right now, and relies on a few gradle tasks to run in specific order:

- run `gradlew generateAccessors` (takenaka -> generateAccessors)
- run `gradlew rebuildStubs` (generate -> rebuildStubs)
- if you are using an IDE reload gradle now, the previous task gerated some subprojects
- run `gradlew build` (build -> build)

## Metadata
VSE provides Spigot metadata on `Player` objects so other plugins can provide special support for handed interactions or somesuch. If you aren’t sure what metadata is, check the [Spigot documentation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/metadata/Metadatable.html). The API supports multiple plugins using the same metadata key, so make sure you filter to our specific plugin name (`Vivecraft-Spigot-Extension`).

For details on available data see the [wiki](https://github.com/Vivecraft/Vivecraft-Spigot-Extension/wiki/Metadata)

## API
We also have an API to access more data, like the history of all available body parts, for more details on that see the [wiki](https://github.com/Vivecraft/Vivecraft-Spigot-Extension/wiki/API).
