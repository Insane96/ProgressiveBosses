package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.data.BossComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonAdapter(DragonDefinition.Serializer.class)
public class DragonDefinition {
    private static final ResourceLocation VANILLA_LOOT_TABLE = new ResourceLocation("entities/ender_dragon");

    public byte level;
    public List<BossComponent> components = new ArrayList<>();

    DragonDefinition(byte level, List<BossComponent> components) {
        this.level = level;
        this.components = components;
    }

    public <T extends BossComponent> Optional<T> getComponent(Class<T> componentClass) {
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

    public void onLivingHurt(LivingHurtEvent event, EnderDragon dragon) {
        components.forEach(component -> component.onLivingHurt(event, dragon));
    }

    public void onLivingDeath(LivingDeathEvent event, EnderDragon dragon) {
        components.forEach(component -> component.onLivingDeath(event, dragon));
    }

    @Nullable
    public DragonCrystal crystal;
    @Nullable
    public DragonMinion minion;
    public int xpDropped;
    public ResourceLocation lootTable;

    public DragonDefinition(byte level, DragonCrystal crystal, @Nullable DragonMinion minion, int xpDropped, ResourceLocation lootTable) {
        this.level = level;
        this.crystal = crystal;
        this.minion = minion;
        this.xpDropped = xpDropped;
        this.lootTable = lootTable;
    }

    /*public static void apply(EnderDragon dragon, DragonDefinition stats) {
        dragon.lootTable = null;
        DragonCrystal.moreCrystals(dragon, stats);
        DragonMinion.setupMinionCooldown(dragon, stats);
    }*/

    public static class Serializer implements JsonDeserializer<DragonDefinition> {
        @Override
        public DragonDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            byte level = GsonHelper.getAsByte(json.getAsJsonObject(), "level");
            List<BossComponent> components = BossComponent.deserializeList(json.getAsJsonObject(), "components", context);
            return new DragonDefinition(level, components);
        }
    }
}
