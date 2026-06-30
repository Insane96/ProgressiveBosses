package insane96mcp.progressivebosses.module.wither;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.PBModules;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

@LoadFeature(module = PBModules.WITHER)
public class WitherFeature extends Feature {
	public static final TagKey<Item> WITHER_INVULNERABLE = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProgressiveBosses.MOD_ID, "wither_invulnerable"));
	public static final TagKey<Item> WORLD_INVULNERABLE = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ProgressiveBosses.MOD_ID, "world_invulnerable"));

	@Config(description = "If true, Wither can charge any entity and not just players.")
	public static Boolean allowChargingNonPlayers = false;

	@SubscribeEvent
	public void onSkullPlaced(BlockEvent.EntityPlaceEvent event) {
		if (!this.isEnabled())
			return;
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
            event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event) {
		if (!(event.getSource().getEntity() instanceof PBWither wither)
				|| event.getEntity().level().isClientSide)
			return;

		boolean hasPlacedRose = false;
		if (EventHooks.canEntityGrief(event.getEntity().level(), wither)) {
			BlockPos blockpos = event.getEntity().blockPosition();
			BlockState blockstate = Blocks.WITHER_ROSE.defaultBlockState();
			if (event.getEntity().level().isEmptyBlock(blockpos) && blockstate.canSurvive(event.getEntity().level(), blockpos)) {
				event.getEntity().level().setBlock(blockpos, blockstate, 3);
				hasPlacedRose = true;
			}
		}

		if (!hasPlacedRose) {
			ItemEntity itementity = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(Items.WITHER_ROSE));
			event.getEntity().level().addFreshEntity(itementity);
		}
	}
}
