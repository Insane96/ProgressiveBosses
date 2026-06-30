package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.module.dragon.phase.DragonCrystalRespawnPhase;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonHoldingPatternPhase;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonStrafePlayerPhase;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@JsonAdapter(FlySpeedComponent.Serializer.class)
public class FlySpeedComponent implements DragonComponent {
    @Nullable
    public DragonValue holding;
    @Nullable
    public DragonValue landing;
    @Nullable
    public DragonValue strafe;
    @Nullable
    public DragonValue charge;
    @Nullable
    public DragonValue respawn;

    public float getFlySpeedMultiplier(EnderDragon dragon) {
        EnderDragonPhase<?> currentPhase = dragon.getPhaseManager().getCurrentPhase().getPhase();
        if (currentPhase == PBDragonHoldingPatternPhase.getPhaseType() && holding != null)
            return holding.getValue(dragon);
        else if (currentPhase == EnderDragonPhase.LANDING && landing != null)
            return landing.getValue(dragon);
        else if (currentPhase == EnderDragonPhase.CHARGING_PLAYER && charge != null)
            return charge.getValue(dragon);
        else if (currentPhase == PBDragonStrafePlayerPhase.getPhaseType() && strafe != null)
            return strafe.getValue(dragon);
        else if (currentPhase == DragonCrystalRespawnPhase.getPhaseType() && respawn != null)
            return respawn.getValue(dragon);
        return 1f;
    }

    public static class Serializer implements JsonDeserializer<FlySpeedComponent> {
        @Override
        public FlySpeedComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            FlySpeedComponent sittingComponent = new FlySpeedComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.holding = GsonHelper.getAsObject(jObject, "holding", null, context, DragonValue.class);
            sittingComponent.landing = GsonHelper.getAsObject(jObject, "landing", null, context, DragonValue.class);
            sittingComponent.strafe = GsonHelper.getAsObject(jObject, "strafe", null, context, DragonValue.class);
            sittingComponent.charge = GsonHelper.getAsObject(jObject, "charge", null, context, DragonValue.class);
            sittingComponent.respawn = GsonHelper.getAsObject(jObject, "respawn", null, context, DragonValue.class);
            return sittingComponent;
        }
    }
}
