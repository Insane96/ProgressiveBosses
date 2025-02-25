package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@JsonAdapter(SittingAttackComponent.Serializer.class)
public class SittingAttackComponent implements DragonComponent {
    @Nullable
    public DragonValue acidAmplifier;
    @Nullable
    public DragonValue damageBeforeTakeOff;
    @Nullable
    public DragonValue flamesBeforeTakeOff;
    @Nullable
    public DragonValue roarTime;
    @Nullable
    public DragonValue scanningIdleTime;
    @Nullable
    public DragonValue flamingTime;

    public static class Serializer implements JsonDeserializer<SittingAttackComponent> {
        @Override
        public SittingAttackComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SittingAttackComponent sittingComponent = new SittingAttackComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.acidAmplifier = context.deserialize(jObject.get("acid_amplifier"), DragonValue.class);
            sittingComponent.damageBeforeTakeOff = context.deserialize(jObject.get("damage_before_take_off"), DragonValue.class);
            sittingComponent.flamesBeforeTakeOff = context.deserialize(jObject.get("flames_before_take_off"), DragonValue.class);
            sittingComponent.roarTime = context.deserialize(jObject.get("roar_time"), DragonValue.class);
            sittingComponent.scanningIdleTime = context.deserialize(jObject.get("scanning_idle_time"), DragonValue.class);
            sittingComponent.flamingTime = context.deserialize(jObject.get("flaming_time"), DragonValue.class);
            return sittingComponent;
        }
    }
}
