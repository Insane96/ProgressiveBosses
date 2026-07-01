package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonAdapter(DragonDefinition.Serializer.class)
public class DragonDefinition {
    public byte level;
    public List<DragonComponent> components = new ArrayList<>();

    DragonDefinition(byte level, List<DragonComponent> components) {
        this.level = level;
        this.components = components;
    }

    public <T extends DragonComponent> Optional<T> getComponent(Class<T> componentClass) {
        return components.stream()
                .filter(component -> component.getClass() == componentClass).findFirst()
                .map(componentClass::cast);
    }

    public void apply(EnderDragon dragon) {
        components.forEach(component -> component.apply(dragon));
    }

    public void tick(EnderDragon dragon) {
        components.forEach(component -> component.tick(dragon));
    }

    public void onEntityJoinLevel(EntityJoinLevelEvent event, EnderDragon dragon) {
        components.forEach(component -> component.onEntityJoinLevel(event, dragon));
    }

    public void onLivingHurt(LivingDamageEvent.Pre event, EnderDragon dragon) {
        components.forEach(component -> component.onLivingHurt(event, dragon));
    }

    public void onLivingDeath(LivingDeathEvent event, EnderDragon dragon) {
        components.forEach(component -> component.onLivingDeath(event, dragon));
    }

    @Nullable
    public DragonMinion minion;

    public DragonDefinition(byte level, @Nullable DragonMinion minion) {
        this.level = level;
        this.minion = minion;
    }

    /*public static void apply(EnderDragon dragon, DragonDefinition stats) {
        DragonMinion.setupMinionCooldown(dragon, stats);
    }*/

    public static class Serializer implements JsonDeserializer<DragonDefinition> {
        @Override
        public DragonDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            byte level = GsonHelper.getAsByte(json.getAsJsonObject(), "level");
            List<DragonComponent> components = DragonComponent.deserializeList(json.getAsJsonObject(), "components", context);
            return new DragonDefinition(level, components);
        }
    }
}
