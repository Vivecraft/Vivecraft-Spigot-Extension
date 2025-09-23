package org.vivecraft.compat.entities;

public interface CreeperHelper {

    /**
     * if the given goal is the creeper swell goal
     */
    boolean isSwellGoal(Object goal);

    /**
     * gets a modified swell goal
     */
    Object getCreeperSwellGoal(Object creeper);

    /**
     * returns if the creeper should charge
     */
    boolean creeperVrCheck(Object creeper);
}
