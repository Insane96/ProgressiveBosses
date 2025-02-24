package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.event.PBEventFactory;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

@JsonAdapter(DragonAttack.Serializer.class)
public class DragonAttack {
    public float meleeDamage;
    public float meleeHeadDamage;
    public DragonValue chargeChance;

    public DragonAttack(float meleeDamage, float meleeHeadDamage, DragonValue chargeChance) {
        this.meleeDamage = meleeDamage;
        this.meleeHeadDamage = meleeHeadDamage;
        this.chargeChance = chargeChance;
    }

    public static class Serializer implements JsonSerializer<DragonAttack>, JsonDeserializer<DragonAttack> {
        @Override
        public DragonAttack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new DragonAttack(
                    GsonHelper.getAsFloat(jObject, "melee_damage"),
                    GsonHelper.getAsFloat(jObject, "melee_head_damage"),
                    context.deserialize(jObject.get("charge_chance"), DragonValue.class)
            );
        }

        @Override
        public JsonElement serialize(DragonAttack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("melee_damage", src.meleeDamage);
            jsonObject.addProperty("melee_head_damage", src.meleeHeadDamage);
            jsonObject.add("charge_chance", context.serialize(src.chargeChance));
            return jsonObject;
        }
    }

    public static float meleeDamage(EnderDragon dragon, float originalDamage) {
        DragonDefinition stats = DragonFeature.getDragonDefinition(dragon).orElse(null);
        if (stats == null || stats.attack == null)
            return originalDamage;

        return stats.attack.meleeDamage;
    }

    public static float meleeHeadDamage(EnderDragon dragon, float originalDamage) {
        DragonDefinition stats = DragonFeature.getDragonDefinition(dragon).orElse(null);
        if (stats == null || stats.attack == null)
            return originalDamage;

        return stats.attack.meleeHeadDamage;
    }

    public static boolean isForcedToCharge(EnderDragon dragon) {
        return getForcedToCharge(dragon) > 0;
    }

    public static int getForcedToCharge(EnderDragon dragon) {
        return dragon.getPersistentData().getInt(StrafePlayerComponent.FORCE_CHARGE_TAG);
    }

    public static void setForcedToCharge(EnderDragon dragon, int forcedToCharge) {
        dragon.getPersistentData().putInt(StrafePlayerComponent.FORCE_CHARGE_TAG, forcedToCharge);
    }

    public static boolean shouldCharge(EnderDragon dragon, DragonDefinition stats) {
        if (isForcedToCharge(dragon))
            return true;

        double chance = stats.attack == null ? 0f : stats.attack.chargeChance.getValue(dragon);
        if (chance == 0f)
            return false;

        return dragon.getRandom().nextDouble() < chance;
    }

    public static void charge(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin) {
        event.setNewPhase(EnderDragonPhase.CHARGING_PLAYER);
        if (isForcedToCharge(dragon))
            setForcedToCharge(dragon, getForcedToCharge(dragon) - 1);
        if (forceBegin) {
            DragonPhaseInstance phase = dragon.getPhaseManager().getPhase(event.getNewPhase());
            phase.begin();
            PBEventFactory.onDragonPhaseBegin(dragon, phase);
        }
    }

    public static void onChargeBegin(DragonPhaseEvent.Begin event, EnderDragon dragon) {
        if (event.getPhaseInstance().getPhase() != EnderDragonPhase.CHARGING_PLAYER)
            return;
        Player player = getRandomPlayer(dragon, dragon.level(), 96);
        if (player == null)
            return;
        dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(player.position());
    }

    @Nullable
    public static Player getRandomPlayer(EnderDragon dragon, Level level, int range) {
        List<Player> players = level.getEntitiesOfClass(Player.class, dragon.getBoundingBox().inflate(range));
        if (players.isEmpty())
            return null;

        return players.get(Mth.nextInt(level.random, 0, players.size() - 1));
    }

}
