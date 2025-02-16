package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.ai.DragonMinionAttackGoal;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonAdapter(DragonMinion.Serializer.class)
public class DragonMinion {
    public static final String DRAGON_MINION = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion";
    public static final String DRAGON_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion_cooldown";

    public float health;
    public int spawned;
    public int averageCooldown;
    public int deltaCooldown;
    public float blindingChance;
    public int blindingDuration;

    public DragonMinion(float health, int spawned, int averageCooldown, int deltaCooldown, float blindingChance, int blindingDuration) {
        this.health = health;
        this.spawned = spawned;
        this.averageCooldown = averageCooldown;
        this.deltaCooldown = deltaCooldown;
        this.blindingChance = blindingChance;
        this.blindingDuration = blindingDuration;
    }

    public static class Serializer implements JsonSerializer<DragonMinion>, JsonDeserializer<DragonMinion> {
        @Override
        public DragonMinion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonMinion(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "spawned"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "average_cooldown"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "delta_cooldown"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "blinding_chance"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "blinding_duration"));
        }

        @Override
        public JsonElement serialize(DragonMinion src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("spawned", src.spawned);
            jsonObject.addProperty("average_cooldown", src.averageCooldown);
            jsonObject.addProperty("delta_cooldown", src.deltaCooldown);
            jsonObject.addProperty("blinding_chance", src.blindingChance);
            jsonObject.addProperty("blinding_duration", src.blindingDuration);
            return jsonObject;
        }
    }

    public static void onShulkerSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Shulker shulker))
            return;

        CompoundTag tags = shulker.getPersistentData();
        if (!tags.contains(DRAGON_MINION))
            return;

        setMinionAI(shulker);
    }

    public static void onMinionHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Shulker shulker))
            return;

        CompoundTag compoundNBT = shulker.getPersistentData();
        if (!compoundNBT.contains(DRAGON_MINION))
            return;

        if (event.getSource().getEntity() instanceof EnderDragon)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onMinionDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Shulker shulker)
                || shulker.level().isClientSide)
            return;

        CompoundTag compoundNBT = shulker.getPersistentData();
        if (!compoundNBT.contains(DRAGON_MINION))
            return;

        List<? extends EnderDragon> dragons = ((ServerLevel) shulker.level()).getDragons();
        if (dragons.isEmpty())
            return;
        EnderDragon dragon = dragons.get(0);
        DragonStats stats = DragonFeature.getDragonStats(dragon).orElse(null);
        if (stats == null)
            return;

        DragonAttack.setForcedToStrafe(dragon, DragonAttack.getForcedToStrafe(dragon) + 1);
    }

    public static void setupMinionCooldown(EnderDragon dragon, DragonStats stats) {
        if (stats.minion == null)
            return;
        int cooldown = (int) dragon.getRandom().triangle(stats.minion.averageCooldown, stats.minion.deltaCooldown);
        dragon.getPersistentData().putInt(DRAGON_MINION_COOLDOWN, cooldown);
    }

    public static void tickMinion(EnderDragon dragon) {
        Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
        if (stats.isEmpty())
            return;

        DragonMinion minionStats = stats.get().minion;
        if (minionStats == null
                || minionStats.spawned <= 0)
            return;

        Level level = dragon.level();

        CompoundTag dragonTags = dragon.getPersistentData();
        int cooldown = dragonTags.getInt(DRAGON_MINION_COOLDOWN);
        if (--cooldown > 0) {
            dragonTags.putInt(DRAGON_MINION_COOLDOWN, cooldown);
            return;
        }

        //If there is no player on the main island don't spawn minions
        BlockPos centerPodium = dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB bb = new AABB(centerPodium).inflate(64d);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, bb);

        if (players.isEmpty())
            return;

        cooldown = (int) level.random.triangle(minionStats.averageCooldown, minionStats.deltaCooldown);
        dragonTags.putInt(DRAGON_MINION_COOLDOWN, cooldown);
        for (int i = 0; i < minionStats.spawned; i++) {
            float angle = level.random.nextFloat() * (float) Math.PI * 2f;
            float x = (float) (Math.cos(angle) * (Mth.nextFloat(dragon.getRandom(), 1f, 4f)));
            float z = (float) (Math.sin(angle) * (Mth.nextFloat(dragon.getRandom(), 1f, 4f)));
            float y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 255, z)).getY();
            summonMinion(level, new Vec3(x, y, z), stats.get().level, stats.get().minion);
        }
    }

    public static void summonMinion(Level world, Vec3 pos, byte lvl, DragonMinion minioStats) {
        Shulker shulker = EntityType.SHULKER.create(world);
        if (shulker == null) {
            LogHelper.warn("Failed to summon Dragon Minion");
            return;
        }
        CompoundTag minionTags = shulker.getPersistentData();
        minionTags.putBoolean(DRAGON_MINION, true);
        minionTags.putByte(DragonFeature.LEVEL, lvl);

        minionTags.putBoolean("mobspropertiesrandomness:processed", true);

        shulker.setPos(pos.x, pos.y, pos.z);
        shulker.setCustomName(Component.translatable(DRAGON_MINION));
        shulker.lootTable = BuiltInLootTables.EMPTY;
        shulker.setPersistenceRequired();
        shulker.setVariant(Optional.of(DyeColor.PURPLE));

        MCUtils.applyModifier(shulker, Attributes.FOLLOW_RANGE, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS_UUID, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS, 96, AttributeModifier.Operation.ADDITION);

        world.addFreshEntity(shulker);
    }

    public static void setMinionAI(Shulker shulker) {
        ArrayList<Goal> toRemove = new ArrayList<>();
        shulker.goalSelector.availableGoals.forEach(goal -> {
            if (goal.getGoal() instanceof Shulker.ShulkerAttackGoal)
                toRemove.add(goal.getGoal());
        });
        toRemove.forEach(shulker.goalSelector::removeGoal);
        shulker.goalSelector.addGoal(4, new DragonMinionAttackGoal(shulker, 200));

        toRemove.clear();
        shulker.targetSelector.availableGoals.forEach(goal -> {
            if (goal.getGoal() instanceof NearestAttackableTargetGoal)
                toRemove.add(goal.getGoal());
            if (goal.getGoal() instanceof HurtByTargetGoal)
                toRemove.add(goal.getGoal());
        });
        toRemove.forEach(shulker.targetSelector::removeGoal);

        shulker.targetSelector.addGoal(2, new ILNearestAttackableTargetGoal<>(shulker, Player.class, false).setIgnoreLineOfSight());
        shulker.targetSelector.addGoal(1, new HurtByTargetGoal(shulker, Shulker.class, EnderDragon.class));
    }
}
