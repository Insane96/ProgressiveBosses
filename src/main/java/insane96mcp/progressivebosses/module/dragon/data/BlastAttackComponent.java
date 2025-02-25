package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.event.PBEventFactory;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@JsonAdapter(BlastAttackComponent.Serializer.class)
public class BlastAttackComponent implements DragonComponent, PhaseChanger {
    public DragonValue chance;
    public DragonValue range;
    @Nullable
    public DragonValue knockback;
    public DragonValue chargeUpTime;
    public DragonValue damage;
    public int timeBeforeTakeoff;
    public DragonValue cooldown;

    public static final String LAST_BLAST_TAG = ProgressiveBosses.RESOURCE_PREFIX + "last_blast";
    private static final String FORCE_BLAST_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_blast";

    public static boolean isForcedToBlast(EnderDragon dragon) {
        return dragon.getPersistentData().getBoolean(FORCE_BLAST_TAG);
    }

    public static void setForcedToBlast(EnderDragon dragon, boolean forcedToBlast) {
        dragon.getPersistentData().putBoolean(FORCE_BLAST_TAG, forcedToBlast);
    }

    public static void blast(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin) {
        if (event.getOldPhase() != EnderDragonPhase.HOVERING &&
                (dragon.getPhaseManager().getPhase(event.getOldPhase()).isSitting() || dragon.getPhaseManager().getPhase(event.getNewPhase()).isSitting())) {
            event.setNewPhase(DragonBlastAttackPhase.getPhaseType());
            if (forceBegin) {
                DragonPhaseInstance phase = dragon.getPhaseManager().getPhase(event.getNewPhase());
                phase.begin();
                PBEventFactory.onDragonPhaseBegin(dragon, phase);
            }
            setForcedToBlast(dragon, false);
        }
        else
            event.setNewPhase(EnderDragonPhase.LANDING);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public EnderDragonPhase<?> getPhase() {
        return DragonBlastAttackPhase.getPhaseType();
    }

    @Override
    public boolean shouldExecute(EnderDragon dragon) {
        if (DragonBlastAttackPhase.isInCooldown(dragon, dragon.level(), this))
            return false;
        if (DragonFeature.getRandomPlayer(dragon, dragon.level(), this.range.getIntValue(dragon)) == null)
            return false;
        if (isForcedToBlast(dragon))
            return true;

        double chance = this.chance.getValue(dragon);
        if (chance == 0f)
            return false;
        return dragon.getRandom().nextDouble() < chance;
    }

    @Override
    public void execute(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin) {
        BlastAttackComponent.setForcedToBlast(dragon, true);
    }

    public static class Serializer implements JsonDeserializer<BlastAttackComponent> {
        @Override
        public BlastAttackComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BlastAttackComponent sittingComponent = new BlastAttackComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.chance = GsonHelper.getAsObject(jObject, "chance", context, DragonValue.class);
            sittingComponent.range = GsonHelper.getAsObject(jObject, "range", context, DragonValue.class);
            sittingComponent.knockback = GsonHelper.getAsObject(jObject, "knockback", null, context, DragonValue.class);
            sittingComponent.chargeUpTime = GsonHelper.getAsObject(jObject, "charge_up_time", context, DragonValue.class);
            sittingComponent.damage = GsonHelper.getAsObject(jObject, "damage", context, DragonValue.class);
            sittingComponent.timeBeforeTakeoff = GsonHelper.getAsInt(jObject, "time_before_takeoff", 0);
            sittingComponent.cooldown = GsonHelper.getAsObject(jObject, "cooldown", context, DragonValue.class);
            return sittingComponent;
        }
    }
}
