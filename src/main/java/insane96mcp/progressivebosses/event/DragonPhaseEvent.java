package insane96mcp.progressivebosses.event;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;

public abstract class DragonPhaseEvent extends LivingEvent {
    EnderDragon dragon;

    public DragonPhaseEvent(EnderDragon dragon) {
        super(dragon);
        this.dragon = dragon;
    }

    public EnderDragon getDragon() {
        return this.dragon;
    }

    /**
     * Triggered when a dragon is about to change phase. Called server side only. Can be used to change which new phase will begin
     */
    public static class Change extends DragonPhaseEvent {
        @Nullable
        EnderDragonPhase<?> oldPhase;
        EnderDragonPhase<?> newPhase;

        public Change(EnderDragon dragon, @Nullable EnderDragonPhase<?> oldPhase, EnderDragonPhase<?> newPhase) {
            super(dragon);
            this.oldPhase = oldPhase;
            this.newPhase = newPhase;
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

    /**
     * Triggered after DragonPhaseInstance.end() is called
     */
    public static class End extends DragonPhaseEvent {
        DragonPhaseInstance phase;

        public End(EnderDragon dragon, DragonPhaseInstance phase) {
            super(dragon);
            this.phase = phase;
        }

        public DragonPhaseInstance getPhaseInstance() {
            return this.phase;
        }
    }

    /**
     * Triggered after DragonPhaseInstance.begin() is called
     */
    public static class Begin extends DragonPhaseEvent {
        DragonPhaseInstance phase;

        public Begin(EnderDragon dragon, DragonPhaseInstance phase) {
            super(dragon);
            this.phase = phase;
        }

        public DragonPhaseInstance getPhaseInstance() {
            return this.phase;
        }
    }
}
