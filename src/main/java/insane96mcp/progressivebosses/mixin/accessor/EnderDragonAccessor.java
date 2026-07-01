package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnderDragon.class)
public interface EnderDragonAccessor {
    @Accessor
    void setSittingDamageReceived(float sittingDamageReceived);

    @Accessor
    int getGrowlTime();

    @Accessor
    void setGrowlTime(int growlTime);
}
