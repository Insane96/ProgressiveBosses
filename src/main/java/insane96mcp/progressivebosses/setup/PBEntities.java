package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.entity.Larva;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.module.wither.entity.minion.WitherMinion;
import insane96mcp.progressivebosses.module.wither.entity.skull.PBWitherSkull;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PBEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ProgressiveBosses.MOD_ID);

	public static final RegistryObject<EntityType<PBWither>> WITHER = REGISTRY.register("wither", () -> EntityType.Builder.of(PBWither::new, MobCategory.MONSTER)
			.fireImmune()
			.immuneTo(Blocks.WITHER_ROSE)
			.sized(0.7f, 2.4f)
			.clientTrackingRange(8)
			.build("wither"));

	public static final RegistryObject<EntityType<PBWitherSkull>> WITHER_SKULL = REGISTRY.register("wither_skull", () -> EntityType.Builder.of((EntityType<PBWitherSkull> pEntityType, Level pLevel) -> new PBWitherSkull(pEntityType, pLevel), MobCategory.MISC)
			.fireImmune()
			.immuneTo(Blocks.WITHER_ROSE)
			.sized(0.3125F, 0.3125F)
			.clientTrackingRange(4)
			.updateInterval(1)
			.build("wither_skull"));

	public static final RegistryObject<EntityType<WitherMinion>> WITHER_MINION = REGISTRY.register("wither_minion", () -> EntityType.Builder.of(WitherMinion::new, MobCategory.MONSTER)
			.sized(0.55f, 1.5f)
			.fireImmune()
			.immuneTo(Blocks.WITHER_ROSE)
			.clientTrackingRange(8)
			.build("wither_minion"));

	public static final RegistryObject<EntityType<Larva>> LARVA = REGISTRY.register("larva", () -> EntityType.Builder.of(Larva::new, MobCategory.MONSTER)
			.sized(0.6f, 0.45f)
			.clientTrackingRange(8)
			.build("larva"));
}
