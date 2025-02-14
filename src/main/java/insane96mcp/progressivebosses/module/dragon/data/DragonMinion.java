package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.ai.DragonMinionAttackGoal;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DragonMinionHelper;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@JsonAdapter(DragonMinion.Serializer.class)
public class DragonMinion {
    public static final String DRAGON_MINION = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion";
    public static final String DRAGON_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion_cooldown";

    public float health;
    public int spawned;
    public int minCooldown;
    public int maxCooldown;
    public float blindingChance;
    public int blindingDuration;

    public DragonMinion(float health, int spawned, int minCooldown, int maxCooldown, float blindingChance, int blindingDuration) {
        this.health = health;
        this.spawned = spawned;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
        this.blindingChance = blindingChance;
        this.blindingDuration = blindingDuration;
    }

    public static class Serializer implements JsonSerializer<DragonMinion>, JsonDeserializer<DragonMinion> {
        @Override
        public DragonMinion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonMinion(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "spawned"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "min_cooldown"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "max_cooldown"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "blinding_chance"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "blinding_duration"));
        }

        @Override
        public JsonElement serialize(DragonMinion src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("spawned", src.spawned);
            jsonObject.addProperty("min_cooldown", src.minCooldown);
            jsonObject.addProperty("max_cooldown", src.maxCooldown);
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
            event.setAmount(event.getAmount() * 0.1f);
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

        dragon.getPersistentData().putInt(DragonAttack.FORCE_STRAFE_TAG, dragon.getPersistentData().getInt(DragonAttack.FORCE_STRAFE_TAG + 1));
    }

    public static void setupMinionCooldown(EnderDragon dragon, DragonStats stats) {
        if (stats.minion == null)
            return;
        int cooldown = (int) (Mth.nextInt(dragon.getRandom(), stats.minion.minCooldown, stats.minion.maxCooldown) * 0.5d);
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
        if (cooldown > 0) {
            dragonTags.putInt(DRAGON_MINION_COOLDOWN, cooldown - 1);
            return;
        }

        //If there is no player on the main island don't spawn minions
        BlockPos centerPodium = dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB bb = new AABB(centerPodium).inflate(64d);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, bb);

        if (players.isEmpty())
            return;

        cooldown = Mth.nextInt(level.random, minionStats.minCooldown, minionStats.maxCooldown);
        dragonTags.putInt(DRAGON_MINION_COOLDOWN, cooldown - 1);
        List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel((ServerLevel) dragon.level()));
        spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius).reversed());
        for (int i = 0; i < minionStats.spawned; i++) {
            for (SpikeFeature.EndSpike spike : spikes) {
                if (!level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()).isEmpty()
                    || !level.getEntitiesOfClass(Shulker.class, spike.getTopBoundingBox()).isEmpty())
                    continue;
                float x = spike.getCenterX() + 0.5f;
                float z = spike.getCenterZ() + 0.5f;
                float y = spike.getHeight() + 1;
                summonMinion(level, new Vec3(x, y, z), stats.get().level, stats.get().minion);
                break;
            }
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

        boolean isBlindingMinion = world.getRandom().nextDouble() < minioStats.blindingChance;

        shulker.setPos(pos.x, pos.y, pos.z);
        shulker.setCustomName(Component.translatable(DRAGON_MINION));
        shulker.lootTable = BuiltInLootTables.EMPTY;
        shulker.setPersistenceRequired();
        DragonMinionHelper.setMinionColor(shulker, isBlindingMinion);

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
        shulker.goalSelector.addGoal(4, new DragonMinionAttackGoal(shulker, 600));

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

    public static void onBulletTick(ShulkerBullet shulkerBulletEntity) {
        if (!shulkerBulletEntity.level().isClientSide
                || !shulkerBulletEntity.getPersistentData().contains("CustomPotionEffects"))
            return;

        List<MobEffectInstance> mobEffectInstances = PotionUtils.getCustomEffects(shulkerBulletEntity.getPersistentData());
        int color = PotionUtils.getColor(mobEffectInstances);
        double r = (double)(color >> 16 & 255) / 255.0D;
        double g = (double)(color >> 8 & 255) / 255.0D;
        double b = (double)(color >> 0 & 255) / 255.0D;
        shulkerBulletEntity.level().addParticle(ParticleTypes.ENTITY_EFFECT, shulkerBulletEntity.getX(), shulkerBulletEntity.getY(), shulkerBulletEntity.getZ(), r, g, b);
    }
}
