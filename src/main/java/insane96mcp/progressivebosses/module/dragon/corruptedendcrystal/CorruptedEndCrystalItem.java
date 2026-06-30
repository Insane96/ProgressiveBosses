package insane96mcp.progressivebosses.module.dragon.corruptedendcrystal;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CorruptedEndCrystalItem extends EndCrystalItem {
    static TagKey<Block> PLACEABLE_ON = TagKey.create(Registries.BLOCK, ProgressiveBosses.location("corrupted_end_crystal_placeable_on"));

    public CorruptedEndCrystalItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * Called when this item is used when targeting a Block
     */
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.is(PLACEABLE_ON))
            return InteractionResult.FAIL;

        BlockPos aboveClickedPos = clickedPos.above();
        if (!level.isEmptyBlock(aboveClickedPos))
            return InteractionResult.FAIL;

        double d0 = aboveClickedPos.getX();
        double d1 = aboveClickedPos.getY();
        double d2 = aboveClickedPos.getZ();
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));
        if (!list.isEmpty())
            return InteractionResult.FAIL;

        if (level instanceof ServerLevel) {
            CorruptedEndCrystal endCrystal = new CorruptedEndCrystal(level, d0 + 0.5D, d1, d2 + 0.5D);
            endCrystal.setShowBottom(false);
            level.addFreshEntity(endCrystal);
            level.gameEvent(pContext.getPlayer(), GameEvent.ENTITY_PLACE, aboveClickedPos);
            EndDragonFight enddragonfight = ((ServerLevel)level).getDragonFight();
            if (enddragonfight != null)
                enddragonfight.tryRespawn();
        }

        pContext.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);

    }
}
