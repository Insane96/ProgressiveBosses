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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

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

    public static void setupMinionCooldown(EnderDragon dragon, DragonStats stats) {
        int cooldown = (int) (Mth.nextInt(dragon.getRandom(), stats.minion.minCooldown, stats.minion.maxCooldown) * 0.5d);
        dragon.getPersistentData().putInt(DRAGON_MINION_COOLDOWN, cooldown);
    }

    public static void tickMinion(EnderDragon dragon) {
        Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
        if (stats.isEmpty()
                || stats.get().minion.spawned <= 0)
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

        cooldown = Mth.nextInt(level.random, stats.get().minion.minCooldown, stats.get().minion.maxCooldown);
        dragonTags.putInt(DRAGON_MINION_COOLDOWN, cooldown - 1);

        float angle = level.random.nextFloat() * (float) Math.PI * 2f;
        float x = (float) (Math.cos(angle) * (Mth.nextFloat(dragon.getRandom(), 16f, 40f)));
        float z = (float) (Math.sin(angle) * (Mth.nextFloat(dragon.getRandom(), 16f, 40f)));
        float y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 255, z)).getY();
        summonMinion(level, new Vec3(x, y, z), stats.get());
    }

    public static void summonMinion(Level world, Vec3 pos, DragonStats stats) {
        Shulker shulker = EntityType.SHULKER.create(world);
        if (shulker == null) {
            LogHelper.warn("Failed to summon Dragon Minion");
            return;
        }
        CompoundTag minionTags = shulker.getPersistentData();
        minionTags.putBoolean(DRAGON_MINION, true);
        minionTags.putByte(DragonFeature.LEVEL, (byte) stats.level);

        minionTags.putBoolean("mobspropertiesrandomness:processed", true);

        boolean isBlindingMinion = world.getRandom().nextDouble() < stats.minion.blindingChance;

        shulker.setPos(pos.x, pos.y, pos.z);
        shulker.setCustomName(Component.translatable(Strings.Translatable.DRAGON_MINION));
        shulker.lootTable = BuiltInLootTables.EMPTY;
        shulker.setPersistenceRequired();
        DragonMinionHelper.setMinionColor(shulker, isBlindingMinion);

        MCUtils.applyModifier(shulker, Attributes.FOLLOW_RANGE, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS_UUID, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS, 64, AttributeModifier.Operation.ADDITION);

        world.addFreshEntity(shulker);
    }

    public static void setMinionAI(Shulker shulker) {
        ArrayList<Goal> toRemove = new ArrayList<>();
        shulker.goalSelector.availableGoals.forEach(goal -> {
            if (goal.getGoal() instanceof Shulker.ShulkerAttackGoal)
                toRemove.add(goal.getGoal());
        });
        toRemove.forEach(shulker.goalSelector::removeGoal);
        shulker.goalSelector.addGoal(2, new DragonMinionAttackGoal(shulker, 70));

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
        if (!shulkerBulletEntity.level().isClientSide && shulkerBulletEntity.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET)) {
            ((ServerLevel)shulkerBulletEntity.level()).sendParticles(ParticleTypes.ENTITY_EFFECT, shulkerBulletEntity.getX(), shulkerBulletEntity.getY(), shulkerBulletEntity.getZ(), 1, 0d, 0d, 0d, 0d);
        }
    }
}
