package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.data.BossComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

@JsonAdapter(DragonVulnerabilitiesComponent.Serializer.class)
public class DragonVulnerabilitiesComponent implements BossComponent {
    @Nullable
    public DragonValue meleeDamageMultiplierWhenSitting;
    @Nullable
    public DragonValue meleeDamageMultiplierWhenFlying;
    @Nullable
    public DragonValue explosionDamageMultiplier;
    @Nullable
    public DragonValue rangedDamageMultiplier;
    @Nullable
    public DragonValue respawningCrystalDamageMultiplier;

    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> CENTER_PODIUM_PHASES = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING);

    @Override
    public void onLivingHurt(LivingHurtEvent event, EnderDragon dragon) {
        meleeDamageMultiplier(event, dragon);
        rangedDamageMultiplier(event, dragon);
        explosionDamageMultiplier(event, dragon);
    }

    private void meleeDamageMultiplier(LivingHurtEvent event, EnderDragon dragon) {
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity))
            return;
        DragonValue multiplier = CENTER_PODIUM_PHASES.contains(dragon.getPhaseManager().getCurrentPhase().getPhase())
                ? meleeDamageMultiplierWhenSitting
                : meleeDamageMultiplierWhenFlying;
        if (multiplier != null)
            event.setAmount(event.getAmount() * multiplier.getValue(dragon));
    }

    private void rangedDamageMultiplier(LivingHurtEvent event, EnderDragon dragon) {
        if (!(event.getSource().getDirectEntity() instanceof Projectile)
                || this.rangedDamageMultiplier == null)
            return;
        event.setAmount(event.getAmount() * this.rangedDamageMultiplier.getValue(dragon));
    }

    private void explosionDamageMultiplier(LivingHurtEvent event, EnderDragon dragon) {
        if (!(event.getSource().is(DamageTypeTags.IS_EXPLOSION) && !event.getSource().is(DamageTypes.FIREWORKS))
                || this.explosionDamageMultiplier == null)
            return;
        event.setAmount(event.getAmount() * this.explosionDamageMultiplier.getValue(dragon));
    }

    public static class Serializer implements JsonDeserializer<DragonVulnerabilitiesComponent> {
        @Override
        public DragonVulnerabilitiesComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DragonVulnerabilitiesComponent sittingComponent = new DragonVulnerabilitiesComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.meleeDamageMultiplierWhenSitting = context.deserialize(jObject.get("melee_damage_multiplier_when_sitting"), DragonValue.class);
            sittingComponent.meleeDamageMultiplierWhenFlying = context.deserialize(jObject.get("melee_damage_multiplier_when_flying"), DragonValue.class);
            sittingComponent.rangedDamageMultiplier = context.deserialize(jObject.get("ranged_damage_multiplier"), DragonValue.class);
            sittingComponent.explosionDamageMultiplier = context.deserialize(jObject.get("explosion_damage_multiplier"), DragonValue.class);
            sittingComponent.respawningCrystalDamageMultiplier = context.deserialize(jObject.get("respawning_crystal_damage_multiplier"), DragonValue.class);
            return sittingComponent;
        }
    }
}
