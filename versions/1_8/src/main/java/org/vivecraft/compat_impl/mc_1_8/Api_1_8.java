package org.vivecraft.compat_impl.mc_1_8;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.*;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.ApiHelper;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.types.Item;
import org.vivecraft.compat.types.Particles;
import org.vivecraft.util.AABB;
import org.vivecraft.util.MathUtils;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Random;
import java.util.logging.Level;

public class Api_1_8 implements ApiHelper {

    protected final Random random = new Random();

    protected ReflectionField Entity_width;
    protected ReflectionMethod Level_spawnParticle;
    protected ReflectionField EnumParticle_CRIT;
    protected ReflectionField EnumParticle_REDSTONE;
    protected ReflectionField EnumParticle_ITEM_CRACK;

    protected ReflectionMethod Entity_getBoundingBox;
    protected ReflectionField AABB_minX;
    protected ReflectionField AABB_minY;
    protected ReflectionField AABB_minZ;
    protected ReflectionField AABB_maxX;
    protected ReflectionField AABB_maxY;
    protected ReflectionField AABB_maxZ;

    protected ReflectionMethod ItemStack_getItem;
    protected ReflectionMethod Item_getId;

    public Api_1_8() {
        this.init();
        this.initAABB();
    }

    protected void init() {
        this.Entity_width = ReflectionField.getField(EntityMapping.FIELD_WIDTH);
        this.Level_spawnParticle = ReflectionMethod.getMethod(ServerLevelMapping.METHOD_FUNC_180505_A);
        this.EnumParticle_CRIT = ReflectionField.getField(EnumParticleMapping.FIELD_CRIT);
        this.EnumParticle_REDSTONE = ReflectionField.getField(EnumParticleMapping.FIELD_REDSTONE);
        this.EnumParticle_ITEM_CRACK = ReflectionField.getField(EnumParticleMapping.FIELD_ITEM_CRACK);
        this.ItemStack_getItem = ReflectionMethod.getMethod(ItemStackMapping.METHOD_GET_ITEM);
        this.Item_getId = ReflectionMethod.getMethod(ItemMapping.METHOD_GET_ID);
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

    @Override
    public <T> void spawnParticle(
        Particles particle, World world, Vector pos, int count, Vector data, double speed, boolean force, T pData)
    {
        if (particle == Particles.CRIT) {
            this.Level_spawnParticle.invoke(BukkitReflector.getWorldHandle(world), this.EnumParticle_CRIT.get(), force,
                pos.getX(), pos.getY(), pos.getZ(), count, data.getX(), data.getY(), data.getZ(), speed, null);
        } else if (particle == Particles.DEBUG) {
            this.Level_spawnParticle.invoke(BukkitReflector.getWorldHandle(world), this.EnumParticle_REDSTONE.get(),
                force, pos.getX(), pos.getY(), pos.getZ(), 0, data.getX(), data.getY(), data.getZ(), 1, null);
        } else if (particle == Particles.ITEM_BREAK && pData instanceof ItemStack) {
            int[] intData = new int[]{(int) this.Item_getId.invokes(
                this.ItemStack_getItem.invoke(BukkitReflector.asNMSCopy((ItemStack) pData))), 0};
            this.Level_spawnParticle.invoke(BukkitReflector.getWorldHandle(world), this.EnumParticle_ITEM_CRACK.get(),
                force, pos.getX(), pos.getY(), pos.getZ(), count, data.getX(), data.getY(), data.getZ(), speed,
                intData);
        }
    }

    @Override
    public Sound getBreakingSound() {
        return Sound.ITEM_BREAK;
    }

    @Override
    public boolean hasSmallHitbox(Player player) {
        return player.isSleeping();
    }

    @Override
    public float getEntityWidth(Entity entity) {
        // up to including some versions of 1.11.2 there is no api for that
        return (float) this.Entity_width.get(BukkitReflector.getEntityHandle(entity));
    }

    @Override
    public ItemStack createItemStack(Item item, String translationName, String fallbackName, ItemFlag[] itemFlags) {
        ItemStack stack;
        switch (item) {
            case LEATHER_BOOTS:
                stack = new ItemStack(Material.LEATHER_BOOTS);
                break;
            case PUMPKIN_PIE:
                stack = new ItemStack(Material.PUMPKIN_PIE);
                break;
            case SHEARS:
                stack = new ItemStack(Material.SHEARS);
                break;
            case WATER_POTION:
                stack = new ItemStack(Material.POTION);
                new Potion(PotionType.WATER).apply(stack);
                break;
            default:
                throw new IllegalArgumentException("Unknown item: " + item);
        }
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(fallbackName);
        if (itemFlags != null) {
            for (ItemFlag itemFlag : itemFlags) {
                meta.addItemFlags(itemFlag);
            }
        }
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public boolean setItemStackUnbreakable(ItemStack itemStack, boolean hide) {
        ItemMeta meta = itemStack.getItemMeta();
        try {
            meta.spigot().setUnbreakable(true);
        } catch (NoSuchMethodError e) {
            ViveMain.LOGGER.log(Level.SEVERE, "errors setting unbreakable", e);
            return false;
        }
        if (hide) {
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        itemStack.setItemMeta(meta);
        return true;
    }

    @Override
    public ShapedRecipe createRecipe(ItemStack itemStack, String id) {
        return new ShapedRecipe(itemStack);
    }

    @Override
    public String getCausingEntityName(PlayerDeathEvent event) {
        return "";
    }

    @Override
    public double applyArmorModifiers(double baseArmor, ItemStack itemStack) {
        return baseArmor;
    }

    @Override
    public AABB getEntityAABB(Entity entity) {
        Object aabb = this.Entity_getBoundingBox.invoke(BukkitReflector.getEntityHandle(entity));
        return new AABB(
            (double) this.AABB_minX.get(aabb), (double) this.AABB_minY.get(aabb), (double) this.AABB_minZ.get(aabb),
            (double) this.AABB_maxX.get(aabb), (double) this.AABB_maxY.get(aabb), (double) this.AABB_maxZ.get(aabb));
    }

    @Override
    public ItemStack getHandItem(Player player, VRBodyPart hand) {
        if (hand == VRBodyPart.MAIN_HAND) {
            return player.getItemInHand();
        }
        return null;
    }

    @Override
    public void setHandItem(Player player, VRBodyPart hand, @Nullable ItemStack itemStack) {
        if (itemStack == null) {
            itemStack = new ItemStack(Material.AIR);
        }
        if (hand == VRBodyPart.MAIN_HAND) {
            player.setItemInHand(itemStack);
        }
    }

    @Override
    public boolean isShield(ItemStack itemStack) {
        // no shields in 1.8
        return false;
    }

    @Override
    public boolean hasItemCooldown(Player player, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isArrowPiercing(Arrow arrow) {
        // no piecing till 1.14
        return false;
    }

    @Override
    public boolean addDamage(ItemStack itemStack, int damage) {
        short durability = itemStack.getDurability();
        short newDurability = (short) (durability + damage);
        itemStack.setDurability(newDurability);
        return newDurability >= itemStack.getType().getMaxDurability();
    }

    @Override
    public void breakItem(Player player, VRBodyPart hand) {
        ItemStack itemStack = getHandItem(player, hand);
        breakItemEffects(player, hand, itemStack);
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            setHandItem(player, hand, null);
        }
    }

    @Override
    public void breakItemEffects(Player player, VRBodyPart hand, ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            Location loc = player.getEyeLocation();
            Vector pPos =
                ViveMain.isVRPlayer(player) ? ViveMain.getVivePlayer(player).getBodyPartPos(hand) : loc.toVector();
            player.playSound(loc, getBreakingSound(), 0.8F, 0.8F + this.random.nextFloat() * 0.4F);
            for (int i = 0; i < 5; i++) {
                Vector3f dir = new Vector3f((this.random.nextFloat() - 0.5F) * 0.1F,
                    (float) Math.random() * 0.1F + 0.1F, 0.0F);
                dir.rotateX(-loc.getPitch() * MathUtils.DEG_TO_RAD);
                dir.rotateY(-loc.getYaw() * MathUtils.DEG_TO_RAD);
                dir.y += 0.05F;
                float y = -this.random.nextFloat() * 0.6F - 0.3F;
                Vector3f pos = new Vector3f();
                if (!ViveMain.isVRPlayer(player)) {
                    pos.set((this.random.nextFloat() - 0.5F) * 0.3F, y, 0.6F);
                    pos.rotateX(-loc.getPitch() * MathUtils.DEG_TO_RAD);
                    pos.rotateY(-loc.getYaw() * MathUtils.DEG_TO_RAD);
                }
                spawnParticle(Particles.ITEM_BREAK, player.getWorld(),
                    new Vector(pPos.getX() + pos.x, pPos.getY() + pos.y, pPos.getZ() + pos.z), 1,
                    MathUtils.toBukkitVec(dir), 0.05, false, itemStack);
            }
        }
    }
}
