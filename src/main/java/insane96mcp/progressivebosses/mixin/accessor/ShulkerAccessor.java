package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Shulker.class)
public interface ShulkerAccessor {
    @Invoker
    void callSetRawPeekAmount(int peekAmount);
}
