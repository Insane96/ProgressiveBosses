package insane96mcp.progressivebosses.module.wither.dispenser;

import insane96mcp.progressivebosses.module.wither.SummonHelper;
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

public class WitherSkullDispenseBehavior extends OptionalDispenseItemBehavior {

	public ItemStack execute(BlockSource source, ItemStack stack) {
		Level level = source.getLevel();
		Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
		BlockPos placedPos = source.getPos().relative(direction);
		if (level.isEmptyBlock(placedPos) && SummonHelper.canItemSpawnPBWither(level, placedPos, stack)) {
			level.setBlock(placedPos, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().get2DDataValue() * 4), 3);
			SummonHelper.checkSpawnFromSkullPlacement(level.getBlockState(placedPos), placedPos, level, null);

			stack.shrink(1);
			this.setSuccess(true);
		} else {
			this.setSuccess(ArmorItem.dispenseArmor(source, stack));
		}

		return stack;
	}
}
