package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.util.json.ILGsonHelper;
import insane96mcp.progressivebosses.mixin.accessor.MobAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.storage.loot.LootTable;

import java.lang.reflect.Type;

@JsonAdapter(LootComponent.Serializer.class)
public class LootComponent implements DragonComponent {
    public Integer xpDropped;
    public ResourceKey<LootTable> lootTable;
    public boolean dropsEgg;

    public static final ResourceKey<LootTable> VANILLA_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("entities/ender_dragon"));

    @Override
    public void apply(EnderDragon dragon) {
        ((MobAccessor) dragon).setLootTable(null);
    }

    public static class Serializer implements JsonDeserializer<LootComponent> {
        @Override
        public LootComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            LootComponent sittingComponent = new LootComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.xpDropped = ILGsonHelper.getAsNullableInt(jObject, "xp_dropped");
            if (jObject.has("loot_table"))
                sittingComponent.lootTable = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(GsonHelper.getAsString(jObject, "loot_table")));
            sittingComponent.dropsEgg = GsonHelper.getAsBoolean(jObject, "drops_egg", false);
            return sittingComponent;
        }
    }
}
