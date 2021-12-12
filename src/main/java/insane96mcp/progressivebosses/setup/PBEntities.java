package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.entity.WitherMinionEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PBEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ProgressiveBosses.MOD_ID);

	public static final RegistryObject<EntityType<WitherMinionEntity>> WITHER_MINION = ENTITIES.register("wither_minion", () -> EntityType.Builder.of(WitherMinionEntity::new, MobCategory.MONSTER)
			.sized(0.55f, 1.5f)
			.fireImmune()
			.immuneTo(Blocks.WITHER_ROSE)
			.clientTrackingRange(8)
			.build("wither_minion"));
}
