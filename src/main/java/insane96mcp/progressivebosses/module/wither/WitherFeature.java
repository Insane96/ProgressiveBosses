package insane96mcp.progressivebosses.module.wither;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@Label(name = "Wither Feature")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither", canBeDisabled = false)
public class WitherFeature extends Feature {
    public static final TagKey<Item> WITHER_INVULNERABLE = ItemTags.create(new ResourceLocation(ProgressiveBosses.MOD_ID, "wither_invulnerable"));

    public WitherFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSkullPlaced(BlockEvent.EntityPlaceEvent event) {
		SummonHelper.checkSpawnFromSkullPlacement(event.getState(), event.getPos(), (Level) event.getLevel(), event.getEntity());
    }

	@SubscribeEvent
	public void onVanillaWitherSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof WitherBoss witherBoss))
			return;

		PBWither wither = PBEntities.WITHER.get().create(event.getLevel());
		if (wither != null) {
			wither.moveTo(witherBoss.position());
			wither.yBodyRot = witherBoss.yBodyRot;
			wither.makeInvulnerable();
			wither.setLvl(0);
            List<ServerPlayer> playersNearby = event.getLevel().getEntitiesOfClass(ServerPlayer.class, wither.getBoundingBox().inflate(50.0D));
			if (wither.stats.misc.netherOnly && event.getLevel().dimension() != Level.NETHER) {
				for (ServerPlayer player : playersNearby) {
					player.sendSystemMessage(Component.translatable(ProgressiveBosses.MOD_ID + ".requires_nether"));
				}
				return;
			}

			for (ServerPlayer serverplayer : playersNearby) {
				CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, wither);
			}

			event.getLevel().addFreshEntity(wither);
		}
		//Couldn't find a better way to discard it (still shows the vanilla boss bar for a second), as discard() doesn't work client-side
		witherBoss.noPhysics = true;
		witherBoss.move(MoverType.SELF, new Vec3(0, -2000, 0));
		witherBoss.kill();
	}
}
