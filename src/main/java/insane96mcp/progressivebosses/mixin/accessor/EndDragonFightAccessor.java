package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EndDragonFight.class)
public interface EndDragonFightAccessor {
    @Accessor
    void setDragonKilled(boolean dragonKilled);
    @Accessor
    ServerLevel getLevel();
}
