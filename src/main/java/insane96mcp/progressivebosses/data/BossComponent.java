package insane96mcp.progressivebosses.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public interface BossComponent {
    default void tick(EnderDragon dragon) {}
    default void apply(EnderDragon dragon) {}
    default void onEntityJoinLevel(EntityJoinLevelEvent event, EnderDragon dragon) {}
    default void onLivingHurt(LivingHurtEvent event, EnderDragon dragon) {}
    default void onLivingDeath(LivingDeathEvent event, EnderDragon dragon) {}

    static List<BossComponent> deserializeList(JsonObject jObject, String memberName, JsonDeserializationContext context) {
        List<BossComponent> components = new ArrayList<>();
        if (!jObject.has(memberName))
            return components;
        JsonArray aModifiers = GsonHelper.getAsJsonArray(jObject, memberName);
        for (JsonElement jsonElement : aModifiers) {
            JsonObject jObjectComponent = jsonElement.getAsJsonObject();
            ResourceLocation componentId = ResourceLocation.tryParse(GsonHelper.getAsString(jObjectComponent, "id"));
            Type component = ComponentRegistry.getComponent(componentId);
            if (component == null) {
                ProgressiveBosses.LOGGER.warn("BossComponent {} does not exist. Skipping", componentId);
                continue;
            }
            components.add(context.deserialize(jObjectComponent, component));
        }
        return components;
    }
}
