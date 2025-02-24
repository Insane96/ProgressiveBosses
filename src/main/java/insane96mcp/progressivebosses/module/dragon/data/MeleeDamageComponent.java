package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.data.BossComponent;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@JsonAdapter(MeleeDamageComponent.Serializer.class)
public class MeleeDamageComponent implements BossComponent {
    @Nullable
    public DragonValue wingDamage;
    @Nullable
    public DragonValue headDamage;

    public static class Serializer implements JsonDeserializer<MeleeDamageComponent> {
        @Override
        public MeleeDamageComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            MeleeDamageComponent sittingComponent = new MeleeDamageComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.wingDamage = GsonHelper.getAsObject(jObject, "wing_damage", context, DragonValue.class);
            sittingComponent.headDamage = GsonHelper.getAsObject(jObject, "head_damage", context, DragonValue.class);
            return sittingComponent;
        }
    }
}
