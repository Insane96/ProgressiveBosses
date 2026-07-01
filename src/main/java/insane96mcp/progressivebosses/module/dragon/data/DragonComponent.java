package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.data.ComponentRegistry;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public interface DragonComponent {
    default void tick(EnderDragon dragon) {}
    default void apply(EnderDragon dragon) {}
    default void onPhaseChange(DragonPhaseEvent.Change event, EnderDragon dragon) {}
    default void onCrystalDestroyed(EnderDragon dragon, EndCrystal endCrystal, int crystalsAlive) {}
    default void onEntityJoinLevel(EntityJoinLevelEvent event, EnderDragon dragon) {}
    default void onLivingHurt(LivingDamageEvent.Pre event, EnderDragon dragon) {}
    default void onLivingDeath(LivingDeathEvent event, EnderDragon dragon) {}

    static List<DragonComponent> deserializeList(JsonObject jObject, String memberName, JsonDeserializationContext context) {
        List<DragonComponent> components = new ArrayList<>();
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
