package org.vivecraft.compat;

import org.vivecraft.compat.entities.CreeperHelper;
import org.vivecraft.compat.entities.EndermanHelper;
import org.vivecraft.linker.Helpers;

public class MCMods {

    private final CreeperHelper creeperHelper;
    private final EndermanHelper endermanHelper;
    private final GameModeHelper gameModeHelper;

    public MCMods() {
        this.creeperHelper = (CreeperHelper) Helpers.getHelper("org.vivecraft.compat_impl.mc_%s.CreeperHelper_%s");
        this.endermanHelper = (EndermanHelper) Helpers.getHelper("org.vivecraft.compat_impl.mc_%s.EndermanHelper_%s");
        this.gameModeHelper = (GameModeHelper) Helpers.getHelper("org.vivecraft.compat_impl.mc_%s.GameModeHelper_%s");
    }

    public EndermanHelper endermanHelper() {
        return this.endermanHelper;
    }

    public CreeperHelper creeperHelper() {
        return this.creeperHelper;
    }

    public GameModeHelper gameModeHelper() {
        return this.gameModeHelper;
    }
}
