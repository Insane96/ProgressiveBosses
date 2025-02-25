package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.util.json.ILGsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import java.lang.reflect.Type;

@JsonAdapter(LootComponent.Serializer.class)
public class LootComponent implements DragonComponent {
    public Integer xpDropped;
    public ResourceLocation lootTable;
    public boolean dropsEgg;

    public static final ResourceLocation VANILLA_LOOT_TABLE = new ResourceLocation("entities/ender_dragon");

    @Override
    public void apply(EnderDragon dragon) {
        dragon.lootTable = null;
    }

    public static class Serializer implements JsonDeserializer<LootComponent> {
        @Override
        public LootComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            LootComponent sittingComponent = new LootComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.xpDropped = ILGsonHelper.getAsNullableInt(jObject, "xp_dropped");
            if (jObject.has("loot_table"))
                sittingComponent.lootTable = new ResourceLocation(GsonHelper.getAsString(jObject, "loot_table"));
            sittingComponent.dropsEgg = GsonHelper.getAsBoolean(jObject, "drops_egg", false);
            return sittingComponent;
        }
    }
}
