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

	public ItemStack execute(IBlockSource source, ItemStack stack) {
		World world = source.getLevel();
		Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
		BlockPos blockpos = source.getPos().relative(direction);
		if (world.isEmptyBlock(blockpos) && WitherSkeletonSkullBlock.canSpawnMob(world, blockpos, stack) && MiscFeature.canPlaceSkull(world, blockpos)) {
			world.setBlock(blockpos, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().get2DDataValue() * 4), 3);
			TileEntity tileentity = world.getBlockEntity(blockpos);
			if (tileentity instanceof SkullTileEntity) {
				WitherSkeletonSkullBlock.checkSpawn(world, blockpos, (SkullTileEntity)tileentity);
			}

			stack.shrink(1);
			this.setSuccess(true);
		} else {
			this.setSuccess(ArmorItem.dispenseArmor(source, stack));
		}

		return stack;
	}
}
