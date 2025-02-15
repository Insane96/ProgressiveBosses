package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystal;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

import java.lang.reflect.Type;
import java.util.Optional;

@JsonAdapter(DragonHealth.Serializer.class)
public class DragonHealth {
    public float health;
    public float regeneration;
    public float crystalRegeneration;
    public float regenWhenHitRatio;
    public int regenWhenHitDuration;

    public DragonHealth(float health, float regeneration, float crystalRegeneration, float regenWhenHitRatio, int regenWhenHitDuration) {
        this.health = health;
        this.regeneration = regeneration;
        this.crystalRegeneration = crystalRegeneration;
        this.regenWhenHitRatio = regenWhenHitRatio;
        this.regenWhenHitDuration = regenWhenHitDuration;
    }

    public static void tryHeal(EnderDragon dragon) {
        if (!dragon.isAlive()
                || dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING
                || dragon.tickCount % 10 != 5)
            return;
        Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
        if (stats.isEmpty())
            return;

        if (stats.get().health.regeneration == 0f)
            return;

        float heal = stats.get().health.regeneration;
        heal /= 2f;

        if (dragon.tickCount - dragon.getLastHurtByMobTimestamp() <= stats.get().health.regenWhenHitDuration)
            heal *= stats.get().health.regenWhenHitRatio;

        dragon.heal(heal);
    }

    public float getHealingFromCrystal(EnderDragon dragon, EndCrystal crystal) {
        if (this.crystalRegeneration == 0f)
            return -1f;

        float heal = this.crystalRegeneration;
        heal /= 2f;

        if (crystal instanceof CorruptedEndCrystal)
            heal *= 2f;

        if (dragon.tickCount - dragon.getLastHurtByMobTimestamp() <= this.regenWhenHitDuration)
            heal *= this.regenWhenHitRatio;

        return heal;
    }

    public static class Serializer implements JsonSerializer<DragonHealth>, JsonDeserializer<DragonHealth> {
        @Override
        public DragonHealth deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonHealth(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "regeneration"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "crystal_regeneration"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "regen_when_hit_ratio"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "regen_when_hit_duration"));
        }

        @Override
        public JsonElement serialize(DragonHealth src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("regeneration", src.regeneration);
            jsonObject.addProperty("crystal_regeneration", src.crystalRegeneration);
            jsonObject.addProperty("regen_when_hit_ratio", src.regenWhenHitRatio);
            jsonObject.addProperty("regen_when_hit_duration", src.regenWhenHitDuration);
            return jsonObject;
        }
    }
}
