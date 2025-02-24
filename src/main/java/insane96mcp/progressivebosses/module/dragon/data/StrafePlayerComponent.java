package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.data.BossComponent;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.event.PBEventFactory;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonStrafePlayerPhase;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

import java.lang.reflect.Type;

@JsonAdapter(StrafePlayerComponent.Serializer.class)
public class StrafePlayerComponent implements BossComponent, PhaseChanger {
    public DragonValue chance;
    public DragonValue minAcidballShot;
    public DragonValue maxAcidballShot;
    public DragonValue cooldownBetweenShots;
    public DragonValue acidballPerShot;

    private static final String FORCE_STRAFE_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_strafe";

    public static boolean isForcedToStrafe(EnderDragon dragon) {
        return getForcedToStrafe(dragon) > 0;
    }

    public static int getForcedToStrafe(EnderDragon dragon) {
        return dragon.getPersistentData().getInt(FORCE_STRAFE_TAG);
    }

    public static void setForcedToStrafe(EnderDragon dragon, int forcedToStrafe) {
        dragon.getPersistentData().putInt(FORCE_STRAFE_TAG, forcedToStrafe);
    }

    public int getAcidballShot(EnderDragon dragon, RandomSource random) {
        int min = Math.max(1, minAcidballShot.getIntValue(dragon));
        int max = Math.max(1, maxAcidballShot.getIntValue(dragon));
        return Mth.nextInt(random, min, max);
    }

    @Override
    public void onPhaseBegin(DragonPhaseEvent.Begin event, EnderDragon dragon) {
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
    public boolean shouldExecute(EnderDragon dragon, DragonDefinition definition) {
        StrafePlayerComponent component = definition.getComponent(StrafePlayerComponent.class).orElse(null);
        if (component == null)
            return false;
        if (DragonFeature.getRandomPlayer(dragon, dragon.level(), 96) == null)
            return false;
        if (isForcedToStrafe(dragon))
            return true;

        double chance = component.chance.getValue(dragon);
        if (chance == 0f)
            return false;
        return dragon.getRandom().nextDouble() < chance;
    }

    @Override
    public void execute(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin) {
        event.setNewPhase(PBDragonStrafePlayerPhase.getPhaseType());
        if (isForcedToStrafe(dragon))
            setForcedToStrafe(dragon, getForcedToStrafe(dragon) - 1);
        if (forceBegin) {
            DragonPhaseInstance phase = dragon.getPhaseManager().getPhase(event.getNewPhase());
            phase.begin();
            PBEventFactory.onDragonPhaseBegin(dragon, phase);
        }
    }

    public static class Serializer implements JsonDeserializer<StrafePlayerComponent> {
        @Override
        public StrafePlayerComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            StrafePlayerComponent sittingComponent = new StrafePlayerComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.chance = GsonHelper.getAsObject(jObject, "chance", context, DragonValue.class);
            sittingComponent.minAcidballShot = GsonHelper.getAsObject(jObject, "min_acidball_shot", context, DragonValue.class);
            sittingComponent.maxAcidballShot = GsonHelper.getAsObject(jObject, "max_acidball_shot", context, DragonValue.class);
            sittingComponent.cooldownBetweenShots = GsonHelper.getAsObject(jObject, "cooldown_between_shots", context, DragonValue.class);
            sittingComponent.acidballPerShot = GsonHelper.getAsObject(jObject, "acidball_per_shot", context, DragonValue.class);
            return sittingComponent;
        }
    }
}
