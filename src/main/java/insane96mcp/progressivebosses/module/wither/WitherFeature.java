package insane96mcp.progressivebosses.module.wither;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.data.*;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Wither Feature")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither", canBeDisabled = false)
public class WitherFeature extends Feature {
	public static final List<WitherStats> DEFAULT_WITHER_STATS = new ArrayList<>(List.of(
			new WitherStats(0,
					new WitherAttack(8f, 1.5f, 0.1f, 50, 60, 2f, 0, 0.05f, 8f, 70, 0.05f, 40, 60, 5),
					new WitherHealth(300f, 1f, 1f, 30),
					new WitherResistancesWeaknesses(new PoweredValue(6f, 13f), new PoweredValue(3f, 4f), 225f),
					new WitherMinionStats(new PoweredValue(1), new PoweredValue(2), new PoweredValue(400, 200), new PoweredValue(500, 250), new PoweredValue(0.1f), 4f, new PoweredValue(0.7f, 0.3f), 0f, 0f, 0f, 0f),
					new WitherMiscStats(7f, false, false, false),
					250, new ResourceLocation("progressivebosses:entities/wither_0")),
			new WitherStats(1,
					new WitherAttack(10f, 1.7f, 0.09f, 45, 55, 2f, 1, 0.05f, 12f, 60, 0.05f, 50, 75, 5),
					new WitherHealth(400f, 1.25f, 1f, 30),
					new WitherResistancesWeaknesses(new PoweredValue(9f, 16f), new PoweredValue(6f, 8f), 250f),
					new WitherMinionStats(new PoweredValue(2), new PoweredValue(4), new PoweredValue(400, 200), new PoweredValue(500, 250), new PoweredValue(0.15f), 4f, new PoweredValue(0.7f, 0.3f), 0.5f, 0.5f, 0.6f, 0.3f),
					new WitherMiscStats(8f, false, true, false),
					600, new ResourceLocation("progressivebosses:entities/wither_1")),
			new WitherStats(2,
					new WitherAttack(13f, 2.1f, 0.075f, 40, 45, 2f, 1, 0.05f, 17f, 50, 0.05f, 50, 90, 4),
					new WitherHealth(500f, 1.5f, 0.8f, 30),
					new WitherResistancesWeaknesses(new PoweredValue(12f, 18f), new PoweredValue(9f, 12f), 275f),
					new WitherMinionStats(new PoweredValue(3), new PoweredValue(6), new PoweredValue(400, 200), new PoweredValue(500, 250), new PoweredValue(0.20f), 4f, new PoweredValue(0.7f, 0.3f), 0.8f, 0.8f, 0.8f, 0.5f),
					new WitherMiscStats(9f, false, true, false),
					950, new ResourceLocation("progressivebosses:entities/wither_2")),
			new WitherStats(3,
					new WitherAttack(16f, 2.5f, 0.05f, 35, 45, 2f, 1, 0.05f, 24f, 40, 0.05f, 50, 100, 3),
					new WitherHealth(600f, 2f, 0.6f, 35),
					new WitherResistancesWeaknesses(new PoweredValue(15f, 20f), new PoweredValue(12f, 16f), 300f),
					new WitherMinionStats(new PoweredValue(5), new PoweredValue(8), new PoweredValue(500, 50), new PoweredValue(700, 350), new PoweredValue(0.25f), 4f, new PoweredValue(0.7f, 0.3f), 1.2f, 1.2f, 1.6f, 0.8f),
					new WitherMiscStats(10f, false, true, true),
					1300, new ResourceLocation("progressivebosses:entities/wither_3"))
	));

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
