package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpikeFeature.class)
public abstract class SpikeFeatureMixin extends Feature<SpikeConfiguration> {

	public SpikeFeatureMixin(Codec<SpikeConfiguration> pCodec) {
		super(pCodec);
	}

	@Inject(method = "placeSpike", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/SpikeFeature;setBlock(Lnet/minecraft/world/level/LevelWriter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", ordinal = 2, shift = At.Shift.AFTER))
	public void progressivebosses$placeSpikeBaseObsidian(ServerLevelAccessor level, RandomSource random, SpikeConfiguration config, SpikeFeature.EndSpike spike, CallbackInfo ci, @Local(name = "k") int x, @Local(name = "l") int z, @Local(name = "i1") int y, @Local(name = "flag") boolean isSideX, @Local(name = "flag1") boolean isSideZ) {
		if (!DragonFeature.areFixesEnabled())
			return;
		if (y == 0 && isSideX && isSideZ) //So if lower corner
			this.setBlock(level, new BlockPos(spike.getCenterX() + x, spike.getHeight() + y - 1, spike.getCenterZ() + z), Blocks.OBSIDIAN.defaultBlockState());
	}
}
