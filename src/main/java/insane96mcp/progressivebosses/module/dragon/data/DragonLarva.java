package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.entity.Larva;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@JsonAdapter(DragonLarva.Serializer.class)
public class DragonLarva {
    public static final String DRAGON_LARVA_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_larva_cooldown";

    public float health;
    public int spawned;
    public int minCooldown;
    public int maxCooldown;

    public DragonLarva(float health, int spawned, int minCooldown, int maxCooldown) {
        this.health = health;
        this.spawned = spawned;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
    }

    public static class Serializer implements JsonSerializer<DragonLarva>, JsonDeserializer<DragonLarva> {
        @Override
        public DragonLarva deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonLarva(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "spawned"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "min_cooldown"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "max_cooldown"));
        }

        @Override
        public JsonElement serialize(DragonLarva src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("spawned", src.spawned);
            jsonObject.addProperty("min_cooldown", src.minCooldown);
            jsonObject.addProperty("max_cooldown", src.maxCooldown);
            return jsonObject;
        }
    }

    public static void setupLarvaCooldown(EnderDragon dragon, DragonStats stats) {
        int cooldown = (int) (Mth.nextInt(dragon.getRandom(), stats.larva.minCooldown, stats.larva.maxCooldown) * 0.5d);
        dragon.getPersistentData().putInt(DRAGON_LARVA_COOLDOWN, cooldown);
    }

    public static void tickLarva(EnderDragon dragon) {
        Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
        if (stats.isEmpty()
                || stats.get().larva.spawned <= 0)
            return;

        CompoundTag dragonTags = dragon.getPersistentData();
        if (dragon.getHealth() <= 0)
            return;

        int cooldown = dragonTags.getInt(DRAGON_LARVA_COOLDOWN);
        if (cooldown > 0) {
            dragonTags.putInt(DRAGON_LARVA_COOLDOWN, cooldown - 1);
            return;
        }

        //If there is no player on the main island don't spawn larvae
        Level level = dragon.level();
        BlockPos centerPodium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB bb = new AABB(centerPodium).inflate(64d);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, bb);

        if (players.isEmpty())
            return;

        cooldown = Mth.nextInt(level.random, stats.get().larva.minCooldown, stats.get().larva.maxCooldown);
        dragonTags.putInt(DRAGON_LARVA_COOLDOWN, cooldown - 1);

        for (int i = 0; i < stats.get().larva.spawned; i++) {
            float angle = level.random.nextFloat() * (float) Math.PI * 2f;
            float x = (float) Math.floor(Math.cos(angle) * 3.33f);
            float z = (float) Math.floor(Math.sin(angle) * 3.33f);
            int y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 255, z)).getY();
            summonLarva(level, new Vec3(x + 0.5, y, z + 0.5), stats.get());
        }
    }

    public static void summonLarva(Level level, Vec3 pos, DragonStats stats) {
        Larva larva = new Larva(PBEntities.LARVA.get(), level);
        CompoundTag minionTags = larva.getPersistentData();

        minionTags.putBoolean("mobspropertiesrandomness:processed", true);
        //TODO Scaling health

        larva.setPos(pos.x, pos.y, pos.z);
        larva.setPersistenceRequired();

        //MCUtils.applyModifier(larva, Attributes.ATTACK_DAMAGE, Strings.AttributeModifiers.ATTACK_DAMAGE_BONUS_UUID, Strings.AttributeModifiers.ATTACK_DAMAGE_BONUS, 0.35, AttributeModifier.Operation.ADDITION);
        MCUtils.applyModifier(larva, ForgeMod.SWIM_SPEED.get(), Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2.5d, AttributeModifier.Operation.MULTIPLY_BASE);
        larva.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.larva.health);
        larva.setHealth(stats.larva.health);

        level.addFreshEntity(larva);
    }
}
