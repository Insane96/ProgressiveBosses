package insane96mcp.progressivebosses.module.wither.dispenser;

import insane96mcp.progressivebosses.module.wither.feature.MiscFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class WitherSkullDispenseBehavior extends OptionalDispenseItemBehavior {

	public ItemStack execute(BlockSource source, ItemStack stack) {
		Level world = source.getLevel();
		Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
		BlockPos blockpos = source.getPos().relative(direction);
		if (world.isEmptyBlock(blockpos) && WitherSkullBlock.canSpawnMob(world, blockpos, stack) && MiscFeature.canPlaceSkull(world, blockpos)) {
			world.setBlock(blockpos, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().get2DDataValue() * 4), 3);
			BlockEntity tileentity = world.getBlockEntity(blockpos);
			if (tileentity instanceof SkullBlockEntity) {
				WitherSkullBlock.checkSpawn(world, blockpos, (SkullBlockEntity)tileentity);
			}

			stack.shrink(1);
			this.setSuccess(true);
		} else {
			this.setSuccess(ArmorItem.dispenseArmor(source, stack));
		}

		return stack;
	}
}
