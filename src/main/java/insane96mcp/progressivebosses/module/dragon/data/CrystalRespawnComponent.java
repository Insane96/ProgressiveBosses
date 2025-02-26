package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.phase.DragonCrystalRespawnPhase;
import insane96mcp.progressivebosses.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

@JsonAdapter(CrystalRespawnComponent.Serializer.class)
public class CrystalRespawnComponent implements DragonComponent, PhaseChanger {

    public static final String DRAGON_PHANTOM = ProgressiveBosses.RESOURCE_PREFIX + "dragon_phantom";
    public static final String PHANTOM_CRYSTAL = ProgressiveBosses.RESOURCE_PREFIX + "crystal";
    public static final String LAST_RESPAWN_TAG = ProgressiveBosses.RESOURCE_PREFIX + "last_respawn";

    public DragonValue minRespawnChance;
    public DragonValue minRespawnChanceAtHealth;
    public DragonValue maxRespawnChance;
    public DragonValue maxRespawnChanceAtHealth;
    public DragonValue respawned;
    @Nullable
    public DragonValue phantomCount;
    public DragonValue phantomSize;
    @Nullable
    public DragonValue cagedChance;
    @Nullable
    public DragonValue corruptedChance;
    @Nullable
    public DragonValue cooldown;

    public static void onPhantomHurt(LivingHurtEvent event) {
        if (!event.getEntity().getPersistentData().contains(DRAGON_PHANTOM)
                || !(event.getSource().getEntity() instanceof EnderDragon))
            return;

        event.setAmount(event.getAmount() * 0.1f);
    }

    public static void tickCrystalPhantom(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide
                || event.getEntity().tickCount % 30 != 0
                || !event.getEntity().getPersistentData().contains(CrystalRespawnComponent.PHANTOM_CRYSTAL))
            return;

        Entity crystal = ((ServerLevel)event.getEntity().level()).getEntity(event.getEntity().getPersistentData().getUUID(CrystalRespawnComponent.PHANTOM_CRYSTAL));
        if (crystal == null || crystal.isRemoved()) {
            if (!event.getEntity().getPersistentData().contains("crystal_death")) {
                event.getEntity().getPersistentData().putLong("crystal_death", event.getEntity().level().getGameTime());
            }
            else if (event.getEntity().level().getGameTime() - event.getEntity().getPersistentData().getLong("crystal_death") > 20 * 30) {
                event.getEntity().kill();
            }
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public EnderDragonPhase<?> getPhase() {
        return DragonCrystalRespawnPhase.getPhaseType();
    }

    @Override
    public boolean shouldExecute(EnderDragon dragon) {
        if (DragonCrystalRespawnPhase.isInCooldown(dragon, dragon.level(), this))
            return false;
        float healthRatio = dragon.getHealth() / dragon.getMaxHealth();

        float chance = Utils.getChanceAtValue(healthRatio, this.minRespawnChanceAtHealth.getValue(dragon), this.maxRespawnChanceAtHealth.getValue(dragon), this.minRespawnChance.getValue(dragon), this.maxRespawnChance.getValue(dragon));

        return dragon.getRandom().nextFloat() < chance;
    }

    @Override
    public void execute(EnderDragon dragon, boolean forceBegin) {
        dragon.getPhaseManager().setPhase(DragonCrystalRespawnPhase.getPhaseType());
        if (forceBegin)
            dragon.getPhaseManager().getPhase(DragonCrystalRespawnPhase.getPhaseType()).begin();
    }

    public static class Serializer implements JsonDeserializer<CrystalRespawnComponent> {
        @Override
        public CrystalRespawnComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            CrystalRespawnComponent component = new CrystalRespawnComponent();
            JsonObject jObject = json.getAsJsonObject();
            component.minRespawnChance = GsonHelper.getAsObject(jObject, "min_respawn_chance", context, DragonValue.class);
            component.minRespawnChanceAtHealth = GsonHelper.getAsObject(jObject, "min_respawn_chance_at_health", context, DragonValue.class);
            component.maxRespawnChance = GsonHelper.getAsObject(jObject, "max_respawn_chance", context, DragonValue.class);
            component.maxRespawnChanceAtHealth = GsonHelper.getAsObject(jObject, "max_respawn_chance_at_health", context, DragonValue.class);
            component.respawned = GsonHelper.getAsObject(jObject, "respawned", context, DragonValue.class);
            component.phantomCount = GsonHelper.getAsObject(jObject, "phantom_count", context, DragonValue.class);
            component.phantomSize = GsonHelper.getAsObject(jObject, "phantom_size", context, DragonValue.class);
            component.cagedChance = GsonHelper.getAsObject(jObject, "caged_chance", context, DragonValue.class);
            component.corruptedChance = GsonHelper.getAsObject(jObject, "corrupted_chance", context, DragonValue.class);
            component.cooldown = GsonHelper.getAsObject(jObject, "cooldown", context, DragonValue.class);
            return component;
        }
    }
}
