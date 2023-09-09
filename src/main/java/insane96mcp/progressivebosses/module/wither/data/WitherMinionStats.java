package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.module.wither.entity.minion.WitherMinion;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Type;
import java.util.List;

@JsonAdapter(WitherMinionStats.Serializer.class)
public class WitherMinionStats {
    public PoweredValue minionsSpawned;
    public PoweredValue maxAround;
    public PoweredValue minCooldown;
    public PoweredValue maxCooldown;
    public PoweredValue bonusMovementSpeed;
    public float magicDamageMultiplier;
    public boolean killMinionOnWitherDeath;
    public PoweredValue bowChance;
    public float sharpnessChance;
    public float knockbackChance;
    public float powerChance;
    public float punchChance;

    public WitherMinionStats(PoweredValue minionsSpawned, PoweredValue maxAround, PoweredValue minCooldown, PoweredValue maxCooldown, PoweredValue bonusMovementSpeed, float magicDamageMultiplier, boolean killMinionOnWitherDeath, PoweredValue bowChance, float sharpnessChance, float knockbackChance, float powerChance, float punchChance) {
        this.minionsSpawned = minionsSpawned;
        this.maxAround = maxAround;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
        this.bonusMovementSpeed = bonusMovementSpeed;
        this.magicDamageMultiplier = magicDamageMultiplier;
        this.killMinionOnWitherDeath = killMinionOnWitherDeath;
        this.bowChance = bowChance;
        this.sharpnessChance = sharpnessChance;
        this.knockbackChance = knockbackChance;
        this.powerChance = powerChance;
        this.punchChance = punchChance;
    }

    public void setCooldown(PBWither wither) {
        wither.minionCooldown = wither.getRandom().nextInt(this.minCooldown.getIntValue(wither), this.maxCooldown.getIntValue(wither));
    }

    public void setCooldown(PBWither wither, float divider) {
        wither.minionCooldown = (int) (wither.getRandom().nextInt(this.minCooldown.getIntValue(wither), this.maxCooldown.getIntValue(wither)) / divider);
    }

    public void trySpawnMinion(PBWither wither) {
        if (wither.isDeadOrDying()
                || wither.getInvulnerableTicks() > 0)
            return;

        //If there is no player in a radius from the wither, don't spawn minions
        int radius = 48;
        BlockPos pos1 = wither.blockPosition().offset(-radius, -radius, -radius);
        BlockPos pos2 = wither.blockPosition().offset(radius, radius, radius);
        AABB bb = new AABB(pos1, pos2);
        List<ServerPlayer> players = wither.level().getEntitiesOfClass(ServerPlayer.class, bb);

        if (players.isEmpty())
            return;

        int minionsCountInAABB = wither.level().getEntitiesOfClass(WitherMinion.class, wither.getBoundingBox().inflate(16)).size();
        if (minionsCountInAABB >= this.maxAround.getIntValue(wither))
            return;

        this.setCooldown(wither);

        int minionSpawnedCount = 0;
        for (int i = 0; i < this.minionsSpawned.getIntValue(wither); i++) {
            int x = 0, y = 0, z = 0;
            //Tries to spawn the Minion up to 5 times
            for (int t = 0; t < 5; t++) {
                x = (int) (wither.getX() + (Mth.nextInt(wither.level().random, -3, 3)));
                y = (int) (wither.getY() + 3);
                z = (int) (wither.getZ() + (Mth.nextInt(wither.level().random, -3, 3)));

                y = MCUtils.getFittingY(PBEntities.WITHER_MINION.get(), new BlockPos(x, y, z), wither.level(), 8);
                if (y != -1)
                    break;
            }
            if (y <= wither.level().getMinBuildHeight())
                continue;

            WitherMinion.create(new Vec3(x + 0.5, y + 0.5, z + 0.5), wither);

            //TODO Kill minions on Wither Death
            /*ListTag minionsList = witherTags.getList(Strings.Tags.MINIONS, Tag.TAG_COMPOUND);
            CompoundTag uuid = new CompoundTag();
            uuid.putUUID("uuid", witherMinion.getUUID());
            minionsList.add(uuid);
            witherTags.put(Strings.Tags.MINIONS, minionsList);*/

            minionsCountInAABB++;
            if (minionsCountInAABB >= this.maxAround.getIntValue(wither))
                break;
        }
    }

    public static class Serializer implements JsonSerializer<WitherMinionStats>, JsonDeserializer<WitherMinionStats> {
        @Override
        public WitherMinionStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherMinionStats(context.deserialize(json.getAsJsonObject().get("minions_spawned"), PoweredValue.class),
                    context.deserialize(json.getAsJsonObject().get("max_minion_around"), PoweredValue.class),
                    context.deserialize(json.getAsJsonObject().get("min_cooldown"), PoweredValue.class),
                    context.deserialize(json.getAsJsonObject().get("max_cooldown"), PoweredValue.class),
                    context.deserialize(json.getAsJsonObject().get("bonus_movement_speed"), PoweredValue.class),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "magic_damage_multiplier"),
                    GsonHelper.getAsBoolean(json.getAsJsonObject(), "kill_minion_on_wither_death"),
                    context.deserialize(json.getAsJsonObject().get("bow_chance"), PoweredValue.class),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "sharpness_chance"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "knockback_chance"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "power_chance"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "punch_chance")
            );
        }

        @Override
        public JsonElement serialize(WitherMinionStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("minions_spawned", context.serialize(src.minionsSpawned));
            jsonObject.add("max_minion_around", context.serialize(src.maxAround));
            jsonObject.add("min_cooldown", context.serialize(src.minCooldown));
            jsonObject.add("max_cooldown", context.serialize(src.maxCooldown));
            jsonObject.add("bonus_movement_speed", context.serialize(src.bonusMovementSpeed));
            jsonObject.add("magic_damage_multiplier", context.serialize(src.magicDamageMultiplier));
            jsonObject.add("kill_minion_on_wither_death", context.serialize(src.killMinionOnWitherDeath));
            jsonObject.add("bow_chance", context.serialize(src.bowChance));
            jsonObject.add("sharpness_chance", context.serialize(src.sharpnessChance));
            jsonObject.add("knockback_chance", context.serialize(src.knockbackChance));
            jsonObject.add("power_chance", context.serialize(src.powerChance));
            jsonObject.add("punch_chance", context.serialize(src.punchChance));
            return jsonObject;
        }
    }
}
