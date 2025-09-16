package org.vivecraft.compat_impl.mc_1_8;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.Vector3f;
import org.vivecraft.accessors.AABBMapping;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.LivingEntityMapping;
import org.vivecraft.accessors.Vec3Mapping;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.NMSHelper;
import org.vivecraft.util.AABB;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_8 implements NMSHelper {

    protected ReflectionField LivingEntity_BodyYaw;

    protected ReflectionMethod Entity_getBoundingBox;
    protected ReflectionField AABB_minX;
    protected ReflectionField AABB_minY;
    protected ReflectionField AABB_minZ;
    protected ReflectionField AABB_maxX;
    protected ReflectionField AABB_maxY;
    protected ReflectionField AABB_maxZ;

    protected ReflectionMethod Entity_getViewVector;
    protected ReflectionField Vec3_X;
    protected ReflectionField Vec3_Y;
    protected ReflectionField Vec3_Z;

    public NMS_1_8() {
        this.init();
        this.initAABB();
        this.initVec3();
    }

    protected void init() {
        this.LivingEntity_BodyYaw = ReflectionField.getField(LivingEntityMapping.FIELD_Y_BODY_ROT);
    }

    protected void initAABB() {
        this.Entity_getBoundingBox = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_BOUNDING_BOX);
        this.AABB_minX = ReflectionField.getField(AABBMapping.FIELD_MIN_X);
        this.AABB_minY = ReflectionField.getField(AABBMapping.FIELD_MIN_Y);
        this.AABB_minZ = ReflectionField.getField(AABBMapping.FIELD_MIN_Z);
        this.AABB_maxX = ReflectionField.getField(AABBMapping.FIELD_MAX_X);
        this.AABB_maxY = ReflectionField.getField(AABBMapping.FIELD_MAX_Y);
        this.AABB_maxZ = ReflectionField.getField(AABBMapping.FIELD_MAX_Z);
    }

    protected void initVec3() {
        this.Entity_getViewVector = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_LOOK_ANGLE);
        this.Vec3_X = ReflectionField.getField(Vec3Mapping.FIELD_X);
        this.Vec3_Y = ReflectionField.getField(Vec3Mapping.FIELD_Y);
        this.Vec3_Z = ReflectionField.getField(Vec3Mapping.FIELD_Z);
    }

    @Override
    public float getLivingEntityBodyYaw(LivingEntity entity) {
        return (float) this.LivingEntity_BodyYaw.get(BukkitReflector.getHandle(entity));
    }

    @Override
    public Vector3f getViewVector(Entity entity) {
        Object vec3 = this.Entity_getViewVector.invoke(BukkitReflector.getHandle(entity));
        return new Vector3f(
            (float) (double) this.Vec3_X.get(vec3),
            (float) (double) this.Vec3_Y.get(vec3),
            (float) (double) this.Vec3_Z.get(vec3));
    }

    @Override
    public AABB getEntityAABB(Entity entity) {
        Object aabb = this.Entity_getBoundingBox.invoke(BukkitReflector.getHandle(entity));
        return new AABB(
            (double) this.AABB_minX.get(aabb), (double) this.AABB_minY.get(aabb), (double) this.AABB_minZ.get(aabb),
            (double) this.AABB_maxX.get(aabb), (double) this.AABB_maxY.get(aabb), (double) this.AABB_maxZ.get(aabb));
    }


    @Override
    public ItemStack setItemStackName(ItemStack itemStack, String translationKey, String fallback) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(fallback);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
