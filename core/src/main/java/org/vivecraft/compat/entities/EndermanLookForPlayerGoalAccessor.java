package org.vivecraft.compat.entities;

public interface EndermanLookForPlayerGoalAccessor {

    Object getEnderman();

    void setPendingTarget(Object pendingTarget);

    Object getPendingTarget();

    void setTarget(Object target);

    Object getTarget();

    void setAggroTime(int aggroTime);

    void setTeleportTime(int teleportTime);

    int getAggroTime();

    int getTeleportTime();

    void superStart();

    // 1.14+
    void setStartAggroConditions(Object condition);

    Object getStartAggroConditions();

    void setContinueAggroConditions(Object condition);

    Object getContinueAggroConditions();
}
