package org.vivecraft.compat_impl.mc_1_21_3;

import org.bukkit.entity.Creaking;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.EntityTypeMapping;
import org.vivecraft.accessors.LevelMapping;
import org.vivecraft.accessors.ServerLevelMapping;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_21.NMS_1_21;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.List;

public class NMS_1_21_3 extends NMS_1_21 {

    protected ReflectionMethod Entity_getEntityType;

    protected ReflectionMethod Entity_restoreFrom;
    protected ReflectionMethod Entity_getPassengers;
    protected ReflectionMethod Entity_ejectPassengers;
    protected ReflectionMethod Entity_startRiding;
    protected ReflectionMethod Entity_getVehicle;
    protected ReflectionMethod Entity_removeAfterChangingDimensions;

    protected ReflectionMethod ServerLevel_addFreshEntity;

    protected ReflectionConstructor VREnderMan_Constructor;
    protected Class<?> VREnderMan;
    protected ReflectionConstructor VRCreaking_Constructor;
    protected Class<?> VRCreaking;

    @Override
    protected void init() {
        super.init();
        this.Entity_getEntityType = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_TYPE);

        this.Entity_restoreFrom = ReflectionMethod.getMethod(EntityMapping.METHOD_RESTORE_FROM);
        this.Entity_getPassengers = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_PASSENGERS);
        this.Entity_ejectPassengers = ReflectionMethod.getMethod(EntityMapping.METHOD_EJECT_PASSENGERS);
        this.Entity_startRiding = ReflectionMethod.getMethod(EntityMapping.METHOD_START_RIDING_1,
            EntityMapping.METHOD_START_RIDING);
        this.Entity_getVehicle = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_VEHICLE);
        this.Entity_removeAfterChangingDimensions = ReflectionMethod.getMethod(
            EntityMapping.METHOD_REMOVE_AFTER_CHANGING_DIMENSIONS);

        this.ServerLevel_addFreshEntity = ReflectionMethod.getMethod(ServerLevelMapping.METHOD_ADD_FRESH_ENTITY);

        this.VREnderMan_Constructor = ReflectionConstructor.getCompat("VREnderMan",
            ClassGetter.getClass(true, EntityTypeMapping.MAPPING), ClassGetter.getClass(true, LevelMapping.MAPPING));
        this.VREnderMan = this.VREnderMan_Constructor.constructor.getDeclaringClass();
        this.VRCreaking_Constructor = ReflectionConstructor.getCompat("VRCreaking",
            ClassGetter.getClass(true, EntityTypeMapping.MAPPING), ClassGetter.getClass(true, LevelMapping.MAPPING));
        this.VRCreaking = this.VRCreaking_Constructor.constructor.getDeclaringClass();
    }

    @Override
    public void modifyEntity(Entity entity) {
        if (entity instanceof Enderman) {
            if (!this.VREnderMan.isInstance(BukkitReflector.getEntityHandle(entity))) {
                Debug.log("replacing Enderman");
                replaceEntity(entity, this.VREnderMan_Constructor);
            }
        } else if (entity instanceof Creaking) {
            if (!this.VRCreaking.isInstance(BukkitReflector.getEntityHandle(entity))) {
                Debug.log("replacing Creaking");
                replaceEntity(entity, this.VRCreaking_Constructor);
            }
        } else {
            super.modifyEntity(entity);
        }
    }

    protected void replaceEntity(Entity bukkitEntity, ReflectionConstructor constructor) {
        Object nmsSource = BukkitReflector.getEntityHandle(bukkitEntity);
        Object replacement = constructor.newInstance(this.Entity_getEntityType.invoke(nmsSource),
            this.Entity_getLevel.invoke(nmsSource));

        // get passengers
        List<?> passengers = (List<?>) this.Entity_getPassengers.invoke(nmsSource);
        this.Entity_ejectPassengers.invoke(nmsSource);

        // copies all the basic data
        this.Entity_restoreFrom.invoke(replacement, nmsSource);

        // remove old entity
        // need to use the dimension change one, because others error since the entities are now linked
        this.Entity_removeAfterChangingDimensions.invoke(nmsSource);

        this.ServerLevel_addFreshEntity.invoke(this.Entity_getLevel.invoke(nmsSource), replacement);

        // restore passengers
        for (Object passenger : passengers) {
            this.startRiding(replacement, passenger, true);
        }
    }

    protected void startRiding(Object vehicle, Object passanger, boolean force) {
        this.Entity_startRiding.invoke(passanger, vehicle, force);
    }
}
