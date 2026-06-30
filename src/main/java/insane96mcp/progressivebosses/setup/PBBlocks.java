package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.block.CorruptedSoulSand;
import insane96mcp.progressivebosses.module.wither.block.CorruptedSoulSandBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PBBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.Blocks.createBlocks(ProgressiveBosses.MOD_ID);

    public static final DeferredBlock<CorruptedSoulSand> CORRUPTED_SOUL_SAND = BLOCKS.register("corrupted_soul_sand", () -> new CorruptedSoulSand(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_SAND)));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ProgressiveBosses.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CorruptedSoulSandBlockEntity>> CORRUPTED_SOUL_SAND_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("corrupted_soul_sand", () -> BlockEntityType.Builder.of(CorruptedSoulSandBlockEntity::new, CORRUPTED_SOUL_SAND.get()).build(null));


}
