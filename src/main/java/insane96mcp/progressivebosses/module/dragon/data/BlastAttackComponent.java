package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.data.BossComponent;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@JsonAdapter(BlastAttackComponent.Serializer.class)
public class BlastAttackComponent implements BossComponent {
    public DragonValue range;
    @Nullable
    public DragonValue knockback;
    public DragonValue chargeUpTime;
    public DragonValue damage;

    public static class Serializer implements JsonDeserializer<BlastAttackComponent> {
        @Override
        public BlastAttackComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BlastAttackComponent sittingComponent = new BlastAttackComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.range = GsonHelper.getAsObject(jObject, "range", context, DragonValue.class);
            sittingComponent.knockback = GsonHelper.getAsObject(jObject, "knockback", null, context, DragonValue.class);
            sittingComponent.chargeUpTime = GsonHelper.getAsObject(jObject, "charge_up_time", context, DragonValue.class);
            sittingComponent.damage = GsonHelper.getAsObject(jObject, "damage", context, DragonValue.class);
            return sittingComponent;
        }
    }
}
