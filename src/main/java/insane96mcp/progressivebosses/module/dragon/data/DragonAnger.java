package insane96mcp.progressivebosses.module.dragon.data;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
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
            DragonAttack.setForcedToBlast(dragon, true);
    }

    public static boolean isAngered(EnderDragon dragon) {
        return dragon.getPersistentData().getBoolean(ANGERED_TAG);
    }

    public static void tickAnger(EnderDragon dragon) {
        float anger = getAnger(dragon);
        if (anger <= 0f || (anger >= MAX_ANGER && !isAngered(dragon)))
            return;
        float tickDown = TICK_DOWN;
        if (isAngered(dragon))
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
    }

    public static void onHurt(LivingHurtEvent event, EnderDragon dragon, DragonStats stats) {
        DragonAnger.addAnger(dragon, event.getAmount() * DAMAGE_TO_ANGER_MODIFIER);
    }

    public static void onCrystalDestroyed(EnderDragon dragon, DragonStats stats) {
        DragonAnger.addAnger(dragon, CRYSTAL_DESTROYED_ANGER);
    }

    public static float flySpeedMultiplier(EnderDragon dragon) {
        return isAngered(dragon) ? 1.3f : 1f;
    }
}
