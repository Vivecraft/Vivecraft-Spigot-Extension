package org.vivecraft.compat.entities;

public interface EndermanHelper {

    /**
     * if the given goal is the enderman freeze goal
     */
    boolean isFreezeGoal(Object goal);

    /**
     * gets a modified freeze goal
     *
     * @apiNote only applicable for 1.14+
     */
    Object getEndermanFreezeWhenLookAt(Object enderman);

    /**
     * sets the goal flags for the freeze goal
     */
    void setFreezeFlags(Object goal);

    /**
     * check if the targeted looks at the enderman, to freeze it
     */
    boolean canFreeze(Object enderman);

    /**
     * makes the enderman look at the targets head
     */
    void lookAtTarget(Object enderman);

    /**
     * stops the enderman from moving
     */
    void stopNavigation(Object enderman);
}
