package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.world.entity.AreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AreaEffectCloud.class)
public interface AreaEffectCloudAccessor {
    @Mutable
    @Accessor
    void setReapplicationDelay(int reapplicationDelay);
}
