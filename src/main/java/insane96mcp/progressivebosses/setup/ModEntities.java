package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.entity.AreaEffectCloud3DEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ProgressiveBosses.MOD_ID);

	public static final RegistryObject<EntityType<AreaEffectCloud3DEntity>> AREA_EFFECT_CLOUD_3D = ENTITIES.register("area_effect_cloud_3d", () -> EntityType.Builder.<AreaEffectCloud3DEntity>create(AreaEffectCloud3DEntity::new, EntityClassification.MISC).immuneToFire().size(6.0F, 0.5F).trackingRange(10).func_233608_b_(Integer.MAX_VALUE).build("area_effect_cloud_3d"));

}
