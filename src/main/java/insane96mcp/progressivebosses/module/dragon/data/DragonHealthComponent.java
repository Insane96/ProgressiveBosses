package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.data.BossComponent;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystal;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@JsonAdapter(DragonHealthComponent.Serializer.class)
public class DragonHealthComponent implements BossComponent {
    @Nullable
    public Integer health;
    @Nullable
    public DragonValue passiveRegeneration;
    @Nullable
    public DragonValue crystalRegeneration;
    @Nullable
    public DragonValue corruptedCrystalRegeneration;
    @Nullable
    public DragonValue regenWhenHitRatio;
    @Nullable
    public DragonValue regenWhenHitDuration;

    /// Returns the regen given reduced if the dragon is currently experiencing reduced regen
    public float reducedRegen(float regen, EnderDragon dragon) {
        return this.regenWhenHitDuration != null
                && this.regenWhenHitRatio != null
                && dragon.getLastHurtByMobTimestamp() < dragon.tickCount && dragon.tickCount - dragon.getLastHurtByMobTimestamp() <= this.regenWhenHitDuration.getValue(dragon) ? regen * this.regenWhenHitRatio.getValue(dragon) : regen;
    }

    @Override
    public void tick(EnderDragon dragon) {
        if (!dragon.isAlive()
                || dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING
                || dragon.tickCount % 10 != 5)
            return;

        if (this.passiveRegeneration == null)
            return;
        float passiveRegen = this.passiveRegeneration.getValue(dragon);
        if (passiveRegen == 0f)
            return;

        //Halved because this is called every 10 ticks
        passiveRegen /= 2f;
        passiveRegen = this.reducedRegen(passiveRegen, dragon);

        dragon.heal(passiveRegen);
    }

    @Override
    public void apply(EnderDragon dragon) {
        if (this.health != null) {
            dragon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.health);
            dragon.setHealth(this.health);
        }
    }

    public float getHealingFromCrystal(EnderDragon dragon, EndCrystal crystal, float original) {
        if (this.crystalRegeneration == null && this.corruptedCrystalRegeneration == null)
            return original;
        boolean isCorrupted = crystal instanceof CorruptedEndCrystal;
        if (this.corruptedCrystalRegeneration == null && isCorrupted)
            return original;
        if (this.crystalRegeneration == null && !isCorrupted)
            return original;

        float heal = isCorrupted ? this.corruptedCrystalRegeneration.getValue(dragon) : this.crystalRegeneration.getValue(dragon);
        heal /= 2f;
        heal = this.reducedRegen(heal, dragon);

        return heal;
    }

    public static class Serializer implements JsonDeserializer<DragonHealthComponent> {
        @Override
        public DragonHealthComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DragonHealthComponent sittingComponent = new DragonHealthComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.health = GsonHelper.getAsInt(jObject, "health");
            sittingComponent.passiveRegeneration = context.deserialize(jObject.get("passive_regeneration"), DragonValue.class);
            sittingComponent.crystalRegeneration = context.deserialize(jObject.get("crystal_regeneration"), DragonValue.class);
            sittingComponent.corruptedCrystalRegeneration = context.deserialize(jObject.get("corrupted_crystal_regeneration"), DragonValue.class);
            sittingComponent.regenWhenHitRatio = context.deserialize(jObject.get("regen_when_hit_ratio"), DragonValue.class);
            sittingComponent.regenWhenHitDuration = context.deserialize(jObject.get("regen_when_hit_duration"), DragonValue.class);
            return sittingComponent;
        }
    }
}
