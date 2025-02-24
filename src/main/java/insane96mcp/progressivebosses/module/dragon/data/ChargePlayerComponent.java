package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.data.BossComponent;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.event.PBEventFactory;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Type;

@JsonAdapter(ChargePlayerComponent.Serializer.class)
public class ChargePlayerComponent implements BossComponent, PhaseChanger {
    public DragonValue chance;

    private static final int RANGE = 96;

    static final String FORCE_CHARGE_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_charge";


    public static boolean isForcedToCharge(EnderDragon dragon) {
        return getForcedToCharge(dragon) > 0;
    }

    public static int getForcedToCharge(EnderDragon dragon) {
        return dragon.getPersistentData().getInt(FORCE_CHARGE_TAG);
    }

    public static void setForcedToCharge(EnderDragon dragon, int forcedToCharge) {
        dragon.getPersistentData().putInt(FORCE_CHARGE_TAG, forcedToCharge);
    }

    @Override
    public void onPhaseBegin(DragonPhaseEvent.Begin event, EnderDragon dragon) {
        if (event.getPhaseInstance().getPhase() != EnderDragonPhase.CHARGING_PLAYER)
            return;
        Player player = DragonFeature.getRandomPlayer(dragon, dragon.level(), RANGE);
        if (player == null)
            return;
        dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(player.position());
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public EnderDragonPhase<?> getPhase() {
        return EnderDragonPhase.CHARGING_PLAYER;
    }

    @Override
    public boolean shouldExecute(EnderDragon dragon) {
        if (DragonFeature.getRandomPlayer(dragon, dragon.level(), RANGE) == null)
            return false;
        if (isForcedToCharge(dragon))
            return true;

        double chance = this.chance.getValue(dragon);
        if (chance == 0f)
            return false;

        return dragon.getRandom().nextDouble() < chance;
    }

    @Override
    public void execute(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin) {
        event.setNewPhase(EnderDragonPhase.CHARGING_PLAYER);
        if (isForcedToCharge(dragon))
            setForcedToCharge(dragon, getForcedToCharge(dragon) - 1);
        if (forceBegin) {
            DragonPhaseInstance phase = dragon.getPhaseManager().getPhase(event.getNewPhase());
            phase.begin();
            PBEventFactory.onDragonPhaseBegin(dragon, phase);
        }
    }

    public static class Serializer implements JsonDeserializer<ChargePlayerComponent> {
        @Override
        public ChargePlayerComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ChargePlayerComponent sittingComponent = new ChargePlayerComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.chance = GsonHelper.getAsObject(jObject, "chance", context, DragonValue.class);
            return sittingComponent;
        }
    }
}
