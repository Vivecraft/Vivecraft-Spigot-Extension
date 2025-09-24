package org.vivecraft.compat_impl.mc_1_8;

import org.vivecraft.ViveMain;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.entities.EndermanHelper;
import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class EndermanHelper_1_8 implements EndermanHelper {

    protected ReflectionMethod Level_getEntitiesOfClass;
    protected ReflectionField NearestAttackableTargetGoal_predicate;
    protected ReflectionField NearestAttackableTargetGoal_comparator;
    protected ReflectionMethod Entity_getBoundingBox;
    protected ReflectionMethod AABB_inflate;
    protected ReflectionMethod Enderman_setCreepy;
    protected ReflectionField Enderman_isAggressive;
    protected ReflectionMethod Enderman_teleport;
    protected ReflectionMethod Enderman_teleportTowards;
    protected ReflectionMethod Entity_makeSound;
    protected ReflectionMethod Mob_lookAt;
    protected ReflectionMethod LivingEntity_getAttribute;
    protected ReflectionField Attributes_MOVEMENT_SPEED;
    protected ReflectionMethod AttributeInstance_removeModifier;
    protected ReflectionMethod AttributeInstance_addModifier;
    protected ReflectionField Enderman_SPEED_MODIFIER_ATTACKING;

    protected ReflectionField Player_inventory;
    protected ReflectionField Inventory_armor;
    protected ReflectionMethod ItemStack_getItem;
    protected ReflectionMethod Item_byBlock;
    protected ReflectionField Blocks_CARVED_PUMPKIN;

    protected ReflectionMethod Mob_getTarget;

    protected ReflectionMethod Entity_distanceToSqr;
    protected Class<?> VREndermanLookForPlayer;
    protected ReflectionConstructor VREndermanLookForPlayer_Constructor;

    protected Class<?> EndermanLookForPlayer;
    protected Class<?> Player;

    public EndermanHelper_1_8() {
        this.init();
        this.initFindPlayer();
        this.initInventory();
    }

    protected void init() {
        this.Mob_getTarget = ReflectionMethod.getMethod(MobMapping.METHOD_GET_TARGET);
        this.Entity_distanceToSqr = ReflectionMethod.getMethod(EntityMapping.METHOD_DISTANCE_TO_SQR);
        this.Mob_lookAt = ReflectionMethod.getMethod(MobMapping.METHOD_LOOK_AT);
        this.Enderman_teleport = ReflectionMethod.getMethod(EnderManMapping.METHOD_TELEPORT);
        this.Enderman_teleportTowards = ReflectionMethod.getMethod(EnderManMapping.METHOD_TELEPORT_TOWARDS);

        this.EndermanLookForPlayer = ClassGetter.getClass(true, EnderMan$EndermanLookForPlayerGoalMapping.MAPPING);

        this.VREndermanLookForPlayer_Constructor = ReflectionConstructor.getCompat(
            "org.vivecraft.compat_impl.mc_X_X.VREndermanLookForPlayerGoal",
            ClassGetter.getClass(true, EnderManMapping.MAPPING));
        this.VREndermanLookForPlayer = this.VREndermanLookForPlayer_Constructor.constructor.getDeclaringClass();

        this.Player = ClassGetter.getClass(true, PlayerMapping.MAPPING);
    }

    protected void initFindPlayer() {
        // this is stupid, but the mapping library doesn't seem to be able to map that one
        try {
            this.Level_getEntitiesOfClass = ReflectionMethod.getMethod(ClassGetter.getClass(true, LevelMapping.MAPPING),
                "a", Class.class, ClassGetter.getClass(true,
                    AABBMapping.MAPPING), ClassGetter.getRaw("com.google.common.base.Predicate"));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        this.NearestAttackableTargetGoal_predicate = ReflectionField.getField(
            NearestAttackableTargetGoalMapping.FIELD_FIELD_82643_G,
            NearestAttackableTargetGoalMapping.FIELD_FIELD_82643_G_1);
        this.NearestAttackableTargetGoal_comparator = ReflectionField.getField(
            NearestAttackableTargetGoalMapping.FIELD_FIELD_75306_G);
        this.Entity_getBoundingBox = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_BOUNDING_BOX);
        this.Enderman_setCreepy = ReflectionMethod.getMethod(EnderManMapping.METHOD_FUNC_70819_E);
        this.Enderman_isAggressive = ReflectionField.getField(EnderManMapping.FIELD_FIELD_104003_G);
        this.LivingEntity_getAttribute = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_GET_ATTRIBUTE);
        this.Attributes_MOVEMENT_SPEED = ReflectionField.getField(AttributesMapping.FIELD_MOVEMENT_SPEED);
        this.AttributeInstance_removeModifier = ReflectionMethod.getMethod(
            AttributeInstanceMapping.METHOD_REMOVE_MODIFIER);
        this.AttributeInstance_addModifier = ReflectionMethod.getMethod(AttributeInstanceMapping.METHOD_ADD_MODIFIER);
        this.Enderman_SPEED_MODIFIER_ATTACKING = ReflectionField.getField(
            EnderManMapping.FIELD_SPEED_MODIFIER_ATTACKING);
        this.Entity_makeSound = ReflectionMethod.getMethod(EntityMapping.METHOD_MAKE_SOUND);
        this.AABB_inflate = ReflectionMethod.getMethod(AABBMapping.METHOD_INFLATE);
    }

    protected void initInventory() {
        this.Player_inventory = ReflectionField.getField(PlayerMapping.FIELD_INVENTORY);
        this.Inventory_armor = ReflectionField.getField(InventoryMapping.FIELD_ARMOR_1, InventoryMapping.FIELD_ARMOR);
        this.ItemStack_getItem = ReflectionMethod.getMethod(ItemStackMapping.METHOD_GET_ITEM);
        this.Item_byBlock = ReflectionMethod.getMethod(ItemMapping.METHOD_BY_BLOCK);
        this.Blocks_CARVED_PUMPKIN = ReflectionField.getField(BlocksMapping.FIELD_CARVED_PUMPKIN, BlocksMapping.FIELD_PUMPKIN);
    }

    protected int adjustedTickDelay(Object goal, int tickDelay) {
        return tickDelay;
    }

    @Override
    public boolean isLookForPlayerGoal(Object goal) {
        return this.EndermanLookForPlayer.isInstance(goal) || this.VREndermanLookForPlayer.isInstance(goal);
    }

    @Override
    public Object getEndermanLookForPlayer(Object enderman) {
        return this.VREndermanLookForPlayer_Constructor.newInstance(enderman);
    }

    @Override
    public boolean lookForPlayerMustSee() {
        // only true for 1.8
        return true;
    }

    @Override
    public void lookForPlayerInit(EndermanLookForPlayerGoalAccessor goal, double distance) {}

    @Override
    public Predicate isAngryAtPredicate(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public Object lookForPlayerNearest(EndermanLookForPlayerGoalAccessor goal, double distance) {
        Object level = ViveMain.NMS.getLevel(goal.getEnderman());
        List players = (List) this.Level_getEntitiesOfClass.invoke(level, this.Player,
            this.AABB_inflate.invoke(this.Entity_getBoundingBox.invoke(goal.getEnderman()), distance, 4.0, distance),
            this.NearestAttackableTargetGoal_predicate.get(goal));
        players.sort((Comparator) this.NearestAttackableTargetGoal_comparator.get(goal));
        return players.isEmpty() ? null : players.get(0);
    }

    @Override
    public void lookForPlayerStart(EndermanLookForPlayerGoalAccessor goal) {
        goal.setAggroTime(this.adjustedTickDelay(goal, 5));
        goal.setTeleportTime(0);
    }

    @Override
    public void lookForPlayerStop(EndermanLookForPlayerGoalAccessor goal) {
        goal.setPendingTarget(null);
        this.Enderman_setCreepy.invoke(goal.getEnderman(), false);
        this.AttributeInstance_removeModifier.invoke(
            this.LivingEntity_getAttribute.invoke(goal.getEnderman(), this.Attributes_MOVEMENT_SPEED.get()),
            this.Enderman_SPEED_MODIFIER_ATTACKING.get());
    }

    @Override
    public Boolean lookForPlayerContinueUse(EndermanLookForPlayerGoalAccessor goal) {
        if (goal.getPendingTarget() != null) {
            if (!this.canPlayerSeeEnderman(goal.getPendingTarget(), goal.getEnderman())) {
                return false;
            }
            this.Enderman_isAggressive.set(goal.getEnderman(), true);
            this.Mob_lookAt.invoke(goal.getEnderman(), goal.getPendingTarget(), 10F, 10F);
            return true;
        }
        return null;
    }

    @Override
    public boolean lookForPlayerTick(EndermanLookForPlayerGoalAccessor goal) {
        if (goal.getPendingTarget() != null) {
            goal.setAggroTime(goal.getAggroTime() - 1);
            if (goal.getAggroTime() <= 0) {
                goal.setTarget(goal.getPendingTarget());
                goal.setPendingTarget(null);
                goal.superStart();
                this.Entity_makeSound.invoke(goal.getEnderman(), "mob.endermen.stare", 1F, 1F);
                this.Enderman_setCreepy.invoke(goal.getEnderman(), true);
                this.AttributeInstance_addModifier.invoke(
                    this.LivingEntity_getAttribute.invoke(goal.getEnderman(), this.Attributes_MOVEMENT_SPEED.get()),
                    this.Enderman_SPEED_MODIFIER_ATTACKING.get());
            }
            return false;
        } else {
            if (goal.getTarget() != null) {
                if (this.Player.isInstance(goal.getTarget()) &&
                    canPlayerSeeEnderman(goal.getTarget(), goal.getEnderman()))
                {
                    if ((double) this.Entity_distanceToSqr.invoke(goal.getTarget(), goal.getEnderman()) < 16.0) {
                        this.Enderman_teleport.invoke(goal.getEnderman());
                    }
                    goal.setTeleportTime(0);
                } else if ((double) this.Entity_distanceToSqr.invoke(goal.getTarget(), goal.getEnderman()) > 256.0) {
                    goal.setTeleportTime(goal.getTeleportTime() + 1);
                    if (goal.getTeleportTime() > this.adjustedTickDelay(goal, 30) &&
                        (boolean) this.Enderman_teleportTowards.invoke(goal.getEnderman(), goal.getTarget()))
                    {
                        goal.setTeleportTime(0);
                    }
                }
            }
            return true;
        }
    }

    @Override
    public boolean isFreezeGoal(Object goal) {
        return false;
    }

    @Override
    public Object getEndermanFreezeWhenLookAt(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public void freezeSetFlags(Object goal) {
        throw new AssertionError();
    }

    @Override
    public boolean freezeCanUse(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public void freezeTick(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public void freezeStart(Object enderman) {
        throw new AssertionError();
    }

    @Override
    public boolean hasProtection(Object nmsPlayer) {
        Object headitem = ((Object[]) this.Inventory_armor.get(this.Player_inventory.get(nmsPlayer)))[3];
        return headitem != null &&
            this.ItemStack_getItem.invoke(headitem) == this.Item_byBlock.invokes(this.Blocks_CARVED_PUMPKIN.get());
    }

    protected boolean canPlayerSeeEnderman(Object target, Object enderman) {
        return !this.hasProtection(target) &&
            ViveMain.NMS.canSeeEachOther(target, enderman, ViveMain.NMS.isVRPlayer(target) ? 0.1 : 0.025, true, false);
    }
}
