package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.module.dragon.data.AngerComponent;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientNetworkHandler {
    public static void applyDragonAnger(int entityId, boolean isAngry) {
        if (Minecraft.getInstance().level == null)
            return;
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if (entity instanceof EnderDragon dragon)
            AngerComponent.setAngered(dragon, isAngry);
    }

    public static void beginBlastAttack(int entityId, int timeToBlowUp) {
        if (Minecraft.getInstance().level == null)
            return;
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if (entity instanceof EnderDragon dragon)
            dragon.getPhaseManager().getPhase(DragonBlastAttackPhase.getPhaseType()).initBlowUpTick(timeToBlowUp);
    }
}
