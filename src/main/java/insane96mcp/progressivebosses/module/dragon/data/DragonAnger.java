package insane96mcp.progressivebosses.module.dragon.data;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.network.SyncDragonAnger;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class DragonAnger {
    public static final String ANGER_TAG = ProgressiveBosses.RESOURCE_PREFIX + "anger";
    public static final String ANGERED_TAG = ProgressiveBosses.RESOURCE_PREFIX + "angered";

    //TODO Configurable
    public static final float MAX_ANGER = 100f;
    public static final float ANGER_DURATION = 60f;
    public static final float TICK_DOWN = MAX_ANGER / ANGER_DURATION * 0.1f;
    public static final float TICK_DOWN_ANGERED = MAX_ANGER / ANGER_DURATION;
    public static final float DAMAGE_TO_ANGER_MODIFIER = 1f;
    public static final float CRYSTAL_DESTROYED_ANGER = MAX_ANGER * 0.25f;

    public static float getAnger(EnderDragon dragon) {
        return dragon.getPersistentData().getFloat(ANGER_TAG);
    }

    public static void addAnger(EnderDragon dragon, float anger) {
        if (isAngered(dragon))
            return;
        float newAnger = getAnger(dragon) + anger;
        dragon.getPersistentData().putFloat(ANGER_TAG, newAnger);
        if (newAnger >= MAX_ANGER)
            BlastAttackComponent.setForcedToBlast(dragon, true);
    }

    public static boolean isAngered(EnderDragon dragon) {
        return dragon.getPersistentData().getBoolean(ANGERED_TAG);
    }

    public static void tickAnger(EnderDragon dragon) {
        if (dragon.tickCount % 20 != 0)
            return;
        float anger = getAnger(dragon);
        boolean angered = isAngered(dragon);
        if (anger <= 0f) {
            if (angered)
                setAngered(dragon, false);
            return;
        }
        else if (anger >= MAX_ANGER && !angered)
            return;
        float tickDown = TICK_DOWN;
        if (angered)
            tickDown = TICK_DOWN_ANGERED;
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
                    .forEach(player -> SyncDragonAnger.sync(player, dragon, angered));
    }

    public static void onHurt(LivingHurtEvent event, EnderDragon dragon, DragonDefinition stats) {
        DragonAnger.addAnger(dragon, event.getAmount() * DAMAGE_TO_ANGER_MODIFIER);
    }

    public static void onCrystalDestroyed(EnderDragon dragon, DragonDefinition stats) {
        DragonAnger.addAnger(dragon, CRYSTAL_DESTROYED_ANGER);
    }

    public static float flySpeedMultiplier(EnderDragon dragon) {
        return isAngered(dragon) ? 1.3f : 1f;
    }

    public static void tick(EnderDragon dragon) {
        if (!dragon.level().isClientSide)
            DragonAnger.tickAnger(dragon);

        else if (isAngered(dragon) && !dragon.getPhaseManager().getCurrentPhase().isSitting()) {
            dragon.growlTime -= 3;
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
}
