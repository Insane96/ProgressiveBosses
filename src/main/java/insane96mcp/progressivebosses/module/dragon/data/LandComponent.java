package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

import java.lang.reflect.Type;

@JsonAdapter(LandComponent.Serializer.class)
public class LandComponent implements DragonComponent, PhaseChanger {
    public DragonValue chance;

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public EnderDragonPhase<?> getPhase() {
        return EnderDragonPhase.LANDING_APPROACH;
    }

    @Override
    public boolean shouldExecute(EnderDragon dragon) {
        return dragon.getRandom().nextFloat() < this.chance.getValue(dragon);
    }

    @Override
    public void execute(EnderDragon dragon, boolean forceBegin) {
        dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
    }

    public static class Serializer implements JsonDeserializer<LandComponent> {
        @Override
        public LandComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            LandComponent sittingComponent = new LandComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.chance = GsonHelper.getAsObject(jObject, "chance", context, DragonValue.class);
            return sittingComponent;
        }
    }
}
