package insane96mcp.progressivebosses.module.wither.block;

import insane96mcp.progressivebosses.setup.PBBlocks;
import net.minecraft.core.BlockPos;
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

    /**
     * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
     * many blocks change at once. This compound comes back to you clientside in {@link BlockEntity#handleUpdateTag(CompoundTag)}
     */
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.lvl = pTag.getInt("lvl");
    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("lvl", this.lvl);
    }
}
