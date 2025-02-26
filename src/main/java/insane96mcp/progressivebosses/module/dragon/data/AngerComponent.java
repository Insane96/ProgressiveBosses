package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.util.json.ILGsonHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.network.SyncDragonAnger;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@JsonAdapter(AngerComponent.Serializer.class)
public class AngerComponent implements DragonComponent {
    public int maxAnger;
    public int angerDuration;
    @Nullable
    public Float tickDown;
    @Nullable
    public Float damageToAngerRatio;
    /// Values below <= 1 are considered % maxAnger, instead, a flat value.
    @Nullable
    public Float crystalDestroyedAnger;
    public boolean forceAngeredWhenLastCrystalDestroyed;

    public static final String ANGER_TAG = ProgressiveBosses.RESOURCE_PREFIX + "anger";
    public static final String ANGERED_TAG = ProgressiveBosses.RESOURCE_PREFIX + "angered";

    public static float getAnger(EnderDragon dragon) {
        return dragon.getPersistentData().getFloat(ANGER_TAG);
    }

    public void addAnger(EnderDragon dragon, float anger) {
        if (isAngered(dragon))
            return;
        float newAnger = getAnger(dragon) + anger;
        dragon.getPersistentData().putFloat(ANGER_TAG, newAnger);
        if (newAnger >= this.maxAnger) {
            DragonFeature.getDragonDefinition(dragon)
                    .flatMap(stats -> stats.getComponent(BlastAttackComponent.class))
                    .ifPresentOrElse(
                            blastAttackComponent -> BlastAttackComponent.setForcedToBlast(dragon, true),
                            () -> setAngered(dragon, true)
                    );
        }
    }

    public static boolean isAngered(@NotNull EnderDragon dragon) {
        return dragon.getPersistentData().getBoolean(ANGERED_TAG);
    }

    public void tickAnger(EnderDragon dragon) {
        if (dragon.tickCount % 20 != 0)
            return;
        float anger = getAnger(dragon);
        boolean angered = isAngered(dragon);
        if (anger <= 0f) {
            if (angered)
                setAngered(dragon, false);
            return;
        }
        else if (anger >= this.maxAnger && !angered)
            return;
        float tickDown;
        if (angered)
            tickDown = (float) this.maxAnger / this.angerDuration;
        else if (this.tickDown != null)
            tickDown = this.tickDown;
        else
            return;
        anger -= tickDown;
        dragon.getPersistentData().putFloat(ANGER_TAG, anger);
        if (anger <= 0f) {
            dragon.getPersistentData().putFloat(ANGER_TAG, 0f);
            setAngered(dragon, false);
        }
    }

    public static void setAngered(EnderDragon dragon, boolean angered) {
        dragon.getPersistentData().putBoolean(ANGERED_TAG, angered);
        if (!dragon.level().isClientSide)
            ((ServerLevel) dragon.level()).players()
                    .forEach(player -> SyncDragonAnger.sync(player, dragon));
    }

    @Override
    public void tick(EnderDragon dragon) {
        if (!dragon.level().isClientSide)
            this.tickAnger(dragon);
    }

    public static void tickClient(EnderDragon dragon) {
        if (isAngered(dragon) && !dragon.getPhaseManager().getCurrentPhase().isSitting()) {
            dragon.growlTime -= 4;
            Vec3 vec3 = dragon.getHeadLookVector(1.0F).normalize();
            vec3.yRot((-(float) Math.PI / 4F));
            double d0 = dragon.head.getX();
            double d1 = dragon.head.getY(0.5D);
            double d2 = dragon.head.getZ();

            for (int i = 0; i < 4; ++i) {
                RandomSource randomsource = dragon.getRandom();
                double d3 = d0 + randomsource.nextGaussian() / 2.0D;
                double d4 = d1 + randomsource.nextGaussian() / 2.0D;
                double d5 = d2 + randomsource.nextGaussian() / 2.0D;
                Vec3 vec31 = dragon.getDeltaMovement();
                dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, true, d3, d4, d5, -vec3.x * (double) 0.01F + vec31.x, -vec3.y * (double) 0.04F + vec31.y, -vec3.z * (double) 0.01F + vec31.z);
                vec3.yRot(0.19634955F);
            }
        }
    }

    @Override
    public void onPhaseChange(DragonPhaseEvent.Change event, EnderDragon dragon) {
        if (event.getNewPhase().equals(EnderDragonPhase.DYING))
            setAngered(dragon, false);
    }

    @Override
    public void onLivingHurt(LivingHurtEvent event, EnderDragon dragon) {
        if (this.damageToAngerRatio == null)
            return;
        this.addAnger(dragon, this.damageToAngerRatio * event.getAmount());
    }

    @Override
    public void onCrystalDestroyed(EnderDragon dragon, EndCrystal endCrystal, int crystalsAlive) {
        if (this.crystalDestroyedAnger != null)
            this.addAnger(dragon, this.crystalDestroyedAnger <= 1 ? this.crystalDestroyedAnger * this.maxAnger : this.crystalDestroyedAnger);
        if (crystalsAlive == 0 && this.forceAngeredWhenLastCrystalDestroyed)
            DragonFeature.getDragonDefinition(dragon).
                    flatMap(stats -> stats.getComponent(BlastAttackComponent.class))
                    .ifPresent(component -> BlastAttackComponent.setForcedToBlast(dragon, true));
    }

    public static class Serializer implements JsonDeserializer<AngerComponent> {
        @Override
        public AngerComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            AngerComponent sittingComponent = new AngerComponent();
            JsonObject jObject = json.getAsJsonObject();
            sittingComponent.maxAnger = GsonHelper.getAsInt(jObject, "max_anger");
            sittingComponent.angerDuration = GsonHelper.getAsInt(jObject, "anger_duration");
            sittingComponent.tickDown = ILGsonHelper.getAsNullableFloat(jObject, "tick_down");
            sittingComponent.damageToAngerRatio = ILGsonHelper.getAsNullableFloat(jObject, "damage_to_anger_ratio");
            sittingComponent.crystalDestroyedAnger = ILGsonHelper.getAsNullableFloat(jObject, "crystal_destroyed_anger");
            sittingComponent.forceAngeredWhenLastCrystalDestroyed = GsonHelper.getAsBoolean(jObject, "force_angered_when_last_crystal_destroyed", false);
            return sittingComponent;
        }
    }
}
