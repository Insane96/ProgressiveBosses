package insane96mcp.progressivebosses.event;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class PBEventFactory {
    public static EnderDragonPhase<?> onDragonChangePhase(EnderDragon dragon, @Nullable EnderDragonPhase<?> currentPhase, EnderDragonPhase<?> newPhase) {
        DragonPhaseChangeEvent event = new DragonPhaseChangeEvent(dragon, currentPhase, newPhase);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getNewPhase();
    }
}
