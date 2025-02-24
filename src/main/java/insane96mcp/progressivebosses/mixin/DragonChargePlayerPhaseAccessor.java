package insane96mcp.progressivebosses.mixin;

import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DragonChargePlayerPhase.class)
public interface DragonChargePlayerPhaseAccessor {
    @Accessor
    void setTimeSinceCharge(int timeSinceCharge);
}
