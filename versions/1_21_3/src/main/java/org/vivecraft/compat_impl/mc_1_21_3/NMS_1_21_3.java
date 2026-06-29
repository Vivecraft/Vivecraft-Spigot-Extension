package org.vivecraft.compat_impl.mc_1_21_3;

import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.*;
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

    private ReflectionMethod LivingEntity_canDisableShield;
    private ReflectionMethod Player_disableShield;
    private ReflectionMethod Player_disableShield_paper;

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
    protected void initShieldDisable() {
        this.LivingEntity_canDisableShield = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_CAN_DISABLE_SHIELD);
        this.Player_disableShield = ReflectionMethod.getMethod(false, PlayerMapping.METHOD_DISABLE_SHIELD);
        this.Player_disableShield_paper = ReflectionMethod.getRaw(
            ClassGetter.getClass(true, PlayerMapping.MAPPING), "disableShield", false,
            ClassGetter.getClass(true, ItemStackMapping.MAPPING),
            ClassGetter.getClass(true, LivingEntityMapping.MAPPING));
        if (this.Player_disableShield == null && this.Player_disableShield_paper == null) {
            throw new RuntimeException("Player_disableShield and Player_disableShield_paper is null");
        }
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

    @Override
    protected void disableShield(Player player, LivingEntity attacker, ItemStack itemStack) {
        Object nmsStack = BukkitReflector.getItemHandle(itemStack);
        Object nmsAttacker = BukkitReflector.getEntityHandle(attacker);
        Object nmsPlayer = BukkitReflector.getEntityHandle(player);
        if (nmsStack != null && (boolean) this.LivingEntity_canDisableShield.invoke(nmsAttacker)) {
            if (this.Player_disableShield_paper != null) {
                this.Player_disableShield_paper.invoke(nmsPlayer, nmsStack, nmsAttacker);
            } else {
                this.Player_disableShield.invoke(nmsPlayer, nmsStack);
            }
        }
    }
}
