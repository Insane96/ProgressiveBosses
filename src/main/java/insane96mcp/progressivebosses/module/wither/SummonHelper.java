package insane96mcp.progressivebosses.module.wither;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.block.CorruptedSoulSandBlockEntity;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBBlocks;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

import javax.annotation.Nullable;

public class SummonHelper {

    @Nullable
    private static BlockPattern witherPatternFull;
    @Nullable
    private static BlockPattern witherPatternBase;

    public static void checkSpawnFromSkullPlacement(BlockState state, BlockPos pos, Level level, @Nullable Entity placerEntity) {
        boolean isWitherSkeletonSkull = state.is(Blocks.WITHER_SKELETON_SKULL) || state.is(Blocks.WITHER_SKELETON_WALL_SKULL);
        if (!isWitherSkeletonSkull || pos.getY() < level.getMinBuildHeight() || level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL)
            return;

        BlockPattern.BlockPatternMatch blockPatternMatch = getOrCreatePBWitherFull().find(level, pos);
        if (blockPatternMatch == null)
            return;

        PBWither wither = PBEntities.WITHER.get().create(level);
        if (wither != null) {
            int lvl = getLevelFromPatternBlocks(level, blockPatternMatch);
            BlockPos blockpos = blockPatternMatch.getBlock(1, 2, 0).getPos();
            wither.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.55D, (double)blockpos.getZ() + 0.5D, blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
            wither.yBodyRot = blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
            wither.makeInvulnerable();
            wither.setLvl(lvl);
            if (wither.stats.misc.netherOnly && level.dimension() != Level.NETHER) {
                if (placerEntity != null)
                    placerEntity.sendSystemMessage(Component.translatable(ProgressiveBosses.MOD_ID + ".requires_nether"));
                return;
            }

            for (ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, wither.getBoundingBox().inflate(50.0D))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, wither);
            }

            CarvedPumpkinBlock.clearPatternBlocks(level, blockPatternMatch);
            level.addFreshEntity(wither);
            CarvedPumpkinBlock.updatePatternBlocks(level, blockPatternMatch);
        }
    }

    private static BlockPattern getOrCreatePBWitherFull() {
        if (witherPatternFull == null) {
            witherPatternFull = BlockPatternBuilder
                    .start()
                    .aisle("^^^", "#C#", "~#~")
                    .where('#', (blockInWorld) -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
                    .where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL))))
                    .where('~', (blockInWorld) -> blockInWorld.getState().canBeReplaced())
                    .where('C', (blockInWorld) -> blockInWorld.getState().is(PBBlocks.CORRUPTED_SOUL_SAND.get()))
                    .build();
        }

        return witherPatternFull;
    }

    private static BlockPattern getOrCreatePBWitherBase() {
        if (witherPatternBase == null) {
            witherPatternBase = BlockPatternBuilder
                    .start()
                    .aisle("   ", "#C#", "~#~")
                    .where('#', (blockInWorld) -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
                    .where('~', (blockInWorld) -> blockInWorld.getState().isAir())
                    .where('C', (blockInWorld) -> blockInWorld.getState().is(PBBlocks.CORRUPTED_SOUL_SAND.get()))
                    .build();
        }

        return witherPatternBase;
    }

    public static boolean canItemSpawnPBWither(Level pLevel, BlockPos pPos, ItemStack pStack) {
        if (pStack.is(Items.WITHER_SKELETON_SKULL) && pPos.getY() >= pLevel.getMinBuildHeight() + 2 && pLevel.getDifficulty() != Difficulty.PEACEFUL && !pLevel.isClientSide) {
            return getOrCreatePBWitherBase().find(pLevel, pPos) != null;
        } else {
            return false;
        }
    }

    public static int getLevelFromPatternBlocks(Level pLevel, BlockPattern.BlockPatternMatch pPatternMatch) {
        for (int i = 0; i < pPatternMatch.getWidth(); ++i) {
            for (int j = 0; j < pPatternMatch.getHeight(); ++j) {
                BlockInWorld blockinworld = pPatternMatch.getBlock(i, j, 0);
                if (blockinworld.getState().is(PBBlocks.CORRUPTED_SOUL_SAND.get())
                        && (pLevel.getBlockEntity(blockinworld.getPos()) instanceof CorruptedSoulSandBlockEntity corruptedSoulSandBlockEntity)) {
                    return corruptedSoulSandBlockEntity.getLvl();
                }
            }
        }
        return -1;
    }
}
