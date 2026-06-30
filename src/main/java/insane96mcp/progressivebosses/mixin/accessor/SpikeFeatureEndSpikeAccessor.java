package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpikeFeature.EndSpike.class)
public interface SpikeFeatureEndSpikeAccessor {
    @Mutable
    @Accessor
    void setGuarded(boolean guarded);
}
