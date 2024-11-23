package insane96mcp.progressivebosses.event;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;

/**
 * Triggered when a dragon is trying to change phase. Called server side only
 */
public class DragonPhaseChangeEvent extends LivingEvent {
    EnderDragon dragon;
    @Nullable
    EnderDragonPhase<?> oldPhase;
    EnderDragonPhase<?> newPhase;

    public DragonPhaseChangeEvent(EnderDragon dragon, @Nullable EnderDragonPhase<?> oldPhase, EnderDragonPhase<?> newPhase) {
        super(dragon);
        this.dragon = dragon;
        this.oldPhase = oldPhase;
        this.newPhase = newPhase;
    }

    public EnderDragon getDragon() {
        return this.dragon;
    }

    public EnderDragonPhase<?> getNewPhase() {
        return this.newPhase;
    }

    public void setNewPhase(EnderDragonPhase<?> newPhase) {
        this.newPhase = newPhase;
    }

    public EnderDragonPhase<?> getOldPhase() {
        return this.oldPhase;
    }
}
