package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.entity.WitherMinionEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class PBEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ProgressiveBosses.MOD_ID);

	public static final RegistryObject<EntityType<WitherMinionEntity>> WITHER_MINION = ENTITIES.register("wither_minion", () -> EntityType.Builder.create(WitherMinionEntity::new, EntityClassification.MONSTER)
			.size(0.55f, 1.5f)
			.immuneToFire()
			.func_233607_a_(Blocks.WITHER_ROSE)
			.trackingRange(8)
			.build("wither_minion"));
}
