package insane96mcp.progressivebosses.event;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.neoforged.neoforge.common.NeoForge;

public class PBEventFactory {
    public static EnderDragonPhase<?> onDragonChangePhase(EnderDragon dragon,EnderDragonPhase<?> currentPhase, EnderDragonPhase<?> newPhase) {
        DragonPhaseEvent.Change event = new DragonPhaseEvent.Change(dragon, currentPhase, newPhase);
        NeoForge.EVENT_BUS.post(event);
        return event.getNewPhase();
    }

    public static void onDragonPhaseEnd(EnderDragon dragon, DragonPhaseInstance phase) {
        DragonPhaseEvent.End event = new DragonPhaseEvent.End(dragon, phase);
        NeoForge.EVENT_BUS.post(event);
    }

    public static void onDragonPhaseBegin(EnderDragon dragon, DragonPhaseInstance phase) {
        DragonPhaseEvent.Begin event = new DragonPhaseEvent.Begin(dragon, phase);
        NeoForge.EVENT_BUS.post(event);
    }
}
