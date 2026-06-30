package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingFlamingPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DragonSittingFlamingPhase.class)
public interface DragonSittingFlamingPhaseAccessor {
    @Accessor
    int getFlameCount();

    @Accessor
    void setFlameCount(int flameCount);
}
