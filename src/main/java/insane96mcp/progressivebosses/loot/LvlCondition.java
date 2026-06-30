package insane96mcp.progressivebosses.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.elderguardian.ElderGuardianFeature;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBLoot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class LvlCondition implements LootItemCondition {

    public static final MapCodec<LvlCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(c -> c.entityTarget),
            IntRange.CODEC.fieldOf("lvl").forGetter(c -> c.lvl)
        ).apply(instance, LvlCondition::new)
    );

    final LootContext.EntityTarget entityTarget;
    final IntRange lvl;

    LvlCondition(LootContext.EntityTarget entityTarget, IntRange lvl) {
        this.entityTarget = entityTarget;
        this.lvl = lvl;
    }

    @Override
    public LootItemConditionType getType() {
        return PBLoot.LVL.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
        int lvl = -1;
        if (entity instanceof PBWither wither)
            lvl = wither.getLvl();
        else if (entity instanceof EnderDragon dragon)
            lvl = DragonFeature.getDragonLvl(dragon);
        else if (entity instanceof ElderGuardian elderGuardian)
            lvl = ElderGuardianFeature.getGuardianLvl(elderGuardian);
        if (lvl == -1)
            return false;

        return this.lvl.test(lootContext, lvl);
    }

    public static Builder withLvl(LootContext.EntityTarget entityTarget, IntRange lvl) {
        return () -> new LvlCondition(entityTarget, lvl);
    }
}
