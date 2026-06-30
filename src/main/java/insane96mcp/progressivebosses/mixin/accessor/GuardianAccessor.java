package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Guardian.class)
public interface GuardianAccessor {
    @Accessor
    void setClientSideAttackTime(int clientSideAttackTime);

    @Invoker
    void callSetActiveAttackTarget(int entityId);
}
