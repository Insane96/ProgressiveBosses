package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystal;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

@JsonAdapter(VulnerabilitiesComponent.Serializer.class)
public class VulnerabilitiesComponent implements DragonComponent {
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
    public float attachedCrystalDamage;
    public AttachedCrystalDamageType attachedCrystalDamageType;
    public float attachedCorruptedCrystalDamage;
    public AttachedCrystalDamageType attachedCorruptedCrystalDamageType;

    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> CENTER_PODIUM_PHASES = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, DragonBlastAttackPhase.getPhaseType());

    public float getAttachedCrystalDamage(EndCrystal endCrystal, EnderDragon dragon) {
        boolean isCorrupted = endCrystal instanceof CorruptedEndCrystal;
        float damage = isCorrupted ? attachedCorruptedCrystalDamage : attachedCrystalDamage;
        AttachedCrystalDamageType type = isCorrupted ? attachedCorruptedCrystalDamageType : attachedCrystalDamageType;

        return damage >= 1f ? damage : (type == AttachedCrystalDamageType.CURRENT_HEALTH ? dragon.getHealth() : dragon.getMaxHealth()) * damage;
    }

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

    public static class Serializer implements JsonDeserializer<VulnerabilitiesComponent> {
        @Override
        public VulnerabilitiesComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            VulnerabilitiesComponent sittingComponent = new VulnerabilitiesComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.meleeDamageMultiplierWhenSitting = context.deserialize(jObject.get("melee_damage_multiplier_when_sitting"), DragonValue.class);
            sittingComponent.meleeDamageMultiplierWhenFlying = context.deserialize(jObject.get("melee_damage_multiplier_when_flying"), DragonValue.class);
            sittingComponent.rangedDamageMultiplier = context.deserialize(jObject.get("ranged_damage_multiplier"), DragonValue.class);
            sittingComponent.explosionDamageMultiplier = context.deserialize(jObject.get("explosion_damage_multiplier"), DragonValue.class);
            sittingComponent.respawningCrystalDamageMultiplier = context.deserialize(jObject.get("respawning_crystal_damage_multiplier"), DragonValue.class);
            sittingComponent.attachedCrystalDamage = GsonHelper.getAsFloat(jObject, "attached_crystal_damage");
            sittingComponent.attachedCrystalDamageType = context.deserialize(jObject.get("attached_crystal_damage_type"), AttachedCrystalDamageType.class);
            sittingComponent.attachedCorruptedCrystalDamage = GsonHelper.getAsFloat(jObject, "attached_corrupted_crystal_damage");
            sittingComponent.attachedCorruptedCrystalDamageType = context.deserialize(jObject.get("attached_corrupted_crystal_damage_type"), AttachedCrystalDamageType.class);
            return sittingComponent;
        }
    }

    public enum AttachedCrystalDamageType {
        @SerializedName("current_health")
        CURRENT_HEALTH,
        @SerializedName("max_health")
        MAX_HEALTH

    }
}
