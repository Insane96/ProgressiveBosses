package insane96mcp.progressivebosses.module.wither.dispenser;

import insane96mcp.progressivebosses.module.wither.feature.MiscFeature;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WitherSkeletonSkullBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WitherSkullDispenseBehavior extends OptionalDispenseBehavior {

	public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
		World world = source.getWorld();
		Direction direction = source.getBlockState().get(DispenserBlock.FACING);
		BlockPos blockpos = source.getBlockPos().offset(direction);
		if (world.isAirBlock(blockpos) && WitherSkeletonSkullBlock.canSpawnMob(world, blockpos, stack) && MiscFeature.canPlaceSkull(world, blockpos)) {
			world.setBlockState(blockpos, Blocks.WITHER_SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, Integer.valueOf(direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().getHorizontalIndex() * 4)), 3);
			TileEntity tileentity = world.getTileEntity(blockpos);
			if (tileentity instanceof SkullTileEntity) {
				WitherSkeletonSkullBlock.checkWitherSpawn(world, blockpos, (SkullTileEntity)tileentity);
			}

			stack.shrink(1);
			this.setSuccessful(true);
		} else {
			this.setSuccessful(ArmorItem.func_226626_a_(source, stack));
		}

		return stack;
	}
}
