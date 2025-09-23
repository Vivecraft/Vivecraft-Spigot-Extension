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
     * if the given goal is the enderman look for player goal
     */
    boolean isLookForPlayerGoal(Object goal);

    /**
     * gets a modified look for player goal
     *
     * @apiNote only applicable for 1.14+
     */
    Object getEndermanLookForPlayer(Object enderman);

    /**
     * sets the goal flags for the freeze goal
     */
    void freezeSetFlags(Object goal);

    /**
     * check if the targeted looks at the enderman, to freeze it
     */
    boolean freezeCanUse(Object enderman);

    /**
     * makes the enderman look at the targets head
     */
    void freezeTick(Object enderman);

    /**
     * stops the enderman from moving
     */
    void freezeStart(Object enderman);

    /**
     * returns if the target must be seen
     *
     * @apiNote only true for 1.8
     */
    boolean lookForPlayerMustSee();

    /**
     * gets the closest player in the area
     */
    Object lookForPlayerNearest(EndermanLookForPlayerGoalAccessor goal, double distance);

    /**
     * starts the agro state
     */
    void lookForPlayerStart(EndermanLookForPlayerGoalAccessor goal);

    /**
     * stops the agro state
     */
    void lookForPlayerStop(EndermanLookForPlayerGoalAccessor goal);

    /**
     * returns if the enderman is allowed to move, return  null if the super check should be done
     */
    Boolean lookForPlayerContinueUse(EndermanLookForPlayerGoalAccessor goal);

    /**
     * teleports the enderman, and returns if the super tick should be called
     */
    boolean lookForPlayerTick(EndermanLookForPlayerGoalAccessor goal);
}
