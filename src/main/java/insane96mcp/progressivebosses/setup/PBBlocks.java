package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.block.CorruptedSoulSand;
import insane96mcp.progressivebosses.module.wither.block.CorruptedSoulSandBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PBBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ProgressiveBosses.MOD_ID);

    public static final RegistryObject<Block> CORRUPTED_SOUL_SAND = BLOCKS.register("corrupted_soul_sand", () -> new CorruptedSoulSand(BlockBehaviour.Properties.copy(Blocks.SOUL_SAND)));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ProgressiveBosses.MOD_ID);
    public static final RegistryObject<BlockEntityType<?>> CORRUPTED_SOUL_SAND_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("corrupted_soul_sand", () -> BlockEntityType.Builder.of(CorruptedSoulSandBlockEntity::new, CORRUPTED_SOUL_SAND.get()).build(null));


}
