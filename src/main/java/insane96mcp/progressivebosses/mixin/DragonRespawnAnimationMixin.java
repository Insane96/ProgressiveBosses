package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "net/minecraft/world/level/dimension/end/DragonRespawnAnimation$4")
public class DragonRespawnAnimationMixin {
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V", ordinal = 0))
    public void onPlayDragonSound(ServerLevel instance, int type, BlockPos blockPos, int flags, Operation<Void> original, ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> crystals, int ticks, BlockPos pos) {
        if (ticks % 5 != 0
                || !Feature.isEnabled(DragonFeature.class)
                || !DragonFeature.enableFixes)
            return;
        original.call(instance, type, blockPos, flags);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;", shift = At.Shift.AFTER))
    public void onDestroyRespawningCrystals(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> crystals, int ticks, BlockPos pos, CallbackInfo ci, @Local EndCrystal crystal) {
        if (!Feature.isEnabled(DragonFeature.class)
                || !DragonFeature.enableFixes)
            return;
        serverLevel.setBlockAndUpdate(crystal.blockPosition(), Blocks.AIR.defaultBlockState());
    }
}
