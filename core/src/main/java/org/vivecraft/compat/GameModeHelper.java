package org.vivecraft.compat;

import org.bukkit.entity.Player;

public interface GameModeHelper {

    /**
     * modifies the gamemode of the player with a vr wrapper, if applicable
     *
     * @param player player object to modify the gamemode of
     * @return true if the gamemode is now a VR gamemode
     */
    boolean modifyGamemode(Player player);

    /**
     * wraps the gamemodes tick
     *
     * @param gameMode gamemode that is ticked
     * @param tick     calls the tick method
     */
    void wrapTick(Object gameMode, Runnable tick);

    /**
     * called after the gamemodes handleBlockBreakAction is called
     *
     * @param gameMode gamemode that is handling the block break action
     */
    void postHandleBlockBreakAction(Object gameMode);
}
