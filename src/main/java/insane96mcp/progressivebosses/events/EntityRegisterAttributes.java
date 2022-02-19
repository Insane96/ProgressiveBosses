package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.entity.LarvaEntity;
import insane96mcp.progressivebosses.module.wither.entity.WitherMinionEntity;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRegisterAttributes {
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(PBEntities.WITHER_MINION.get(), WitherMinionEntity.prepareAttributes().build());
		event.put(PBEntities.LARVA.get(), LarvaEntity.prepareAttributes().build());
	}
}
