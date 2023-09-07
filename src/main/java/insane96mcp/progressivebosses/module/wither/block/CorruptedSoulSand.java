package insane96mcp.progressivebosses.module.wither.block;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CorruptedSoulSand extends BaseEntityBlock {
    public CorruptedSoulSand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(stack, pLevel, pTooltip, pFlag);
        CompoundTag compoundTag = BlockItem.getBlockEntityData(stack);
        if (compoundTag == null)
            return;
        int lvl = compoundTag.getInt("lvl");
        pTooltip.add(Component.translatable(ProgressiveBosses.RESOURCE_PREFIX + "corrupted_soul_sand.corruption_level", lvl).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof CorruptedSoulSandBlockEntity corruptedSoulSandBlockEntity) {
            if (!level.isClientSide && player.isCreative()) {
                ItemStack itemstack = new ItemStack(this.asItem());
                blockentity.saveToItem(itemstack);

                ItemEntity itementity = new ItemEntity(level, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CorruptedSoulSandBlockEntity(pos, state);
    }
}
