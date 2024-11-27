package insane96mcp.progressivebosses.mixin;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Projectile.class)
public interface ProjectileInvoker {
    @Invoker
    void invokeOnHitEntity(EntityHitResult pResult);
    @Invoker
    void invokeOnHitBlock(BlockHitResult pResult);
}
