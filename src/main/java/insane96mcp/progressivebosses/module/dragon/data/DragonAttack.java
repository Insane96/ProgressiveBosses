package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.event.PBEventFactory;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Type;

@JsonAdapter(DragonAttack.Serializer.class)
public class DragonAttack {
    public DragonValue chargeChance;

    public DragonAttack(DragonValue chargeChance) {
        this.chargeChance = chargeChance;
    }

    public static class Serializer implements JsonSerializer<DragonAttack>, JsonDeserializer<DragonAttack> {
        @Override
        public DragonAttack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new DragonAttack(
                    context.deserialize(jObject.get("charge_chance"), DragonValue.class)
            );
        }

        @Override
        public JsonElement serialize(DragonAttack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("charge_chance", context.serialize(src.chargeChance));
            return jsonObject;
        }
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
        Player player = DragonFeature.getRandomPlayer(dragon, dragon.level(), 96);
        if (player == null)
            return;
        dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(player.position());
    }

}
