package insane96mcp.progressivebosses.module.wither.block;

import insane96mcp.progressivebosses.setup.PBBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CorruptedSoulSandBlockEntity extends BlockEntity {
    int lvl;

    public CorruptedSoulSandBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PBBlocks.CORRUPTED_SOUL_SAND_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public int getLvl() {
        return this.lvl;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        this.lvl = pTag.getInt("lvl");
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.putInt("lvl", this.lvl);
    }
}
