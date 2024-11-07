package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpikeFeature.class)
public abstract class SpikeFeatureMixin extends Feature<SpikeConfiguration> {

	public SpikeFeatureMixin(Codec<SpikeConfiguration> pCodec) {
		super(pCodec);
	}

	@ModifyExpressionValue(method = "placeSpike", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/SpikeFeature$EndSpike;isGuarded()Z"))
	public boolean onTryPlaceBars(boolean isGuarded, ServerLevelAccessor level, RandomSource random, SpikeConfiguration configuration, SpikeFeature.EndSpike spike) {
		if (!isGuarded
				|| !insane96mcp.insanelib.base.Feature.isEnabled(DragonFeature.class))
			return isGuarded;

		int j1 = -2;
		int k1 = 2;
		int j = 3;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int x = -2; x <= 2; ++x) {
			for (int z = -2; z <= 2; ++z) {
				for (int y = 0; y <= 3; ++y) {
					boolean isSideX = Mth.abs(x) == 2;
					boolean isSideZ = Mth.abs(z) == 2;
					boolean isRoof = y == 3;
					if (isSideX || isSideZ || isRoof) {
						this.setBlock(level, blockpos$mutableblockpos.set(spike.getCenterX() + x, spike.getHeight() + y, spike.getCenterZ() + z), Blocks.IRON_BARS.defaultBlockState());
						if (y == 0 && isSideX && isSideZ) //So if corner
							this.setBlock(level, blockpos$mutableblockpos.set(spike.getCenterX() + x, spike.getHeight() + y - 1, spike.getCenterZ() + z), Blocks.OBSIDIAN.defaultBlockState());
					}
				}
			}
		}
		return false;
	}
}
