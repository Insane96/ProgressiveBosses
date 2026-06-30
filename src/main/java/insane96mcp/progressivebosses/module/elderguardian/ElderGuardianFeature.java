package insane96mcp.progressivebosses.module.elderguardian;

import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.mixin.accessor.ExplosionAccessor;
import insane96mcp.progressivebosses.mixin.accessor.MobAccessor;
import insane96mcp.progressivebosses.module.PBModules;
import insane96mcp.progressivebosses.module.elderguardian.data.ElderGuardianStats;
import insane96mcp.progressivebosses.module.elderguardian.data.ElderGuardianStatsReloadListener;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@LoadFeature(module = PBModules.ELDER_GUARDIAN)
public class ElderGuardianFeature extends Feature {
	public static final ResourceLocation ELDER_MINION_BONUS = ProgressiveBosses.id("elder_minion_bonus");

	public static ResourceLocation LEVEL;
	public static ResourceLocation PREVIOUSLY_NEAR_ELDER_GUARDIAN;
	public static ResourceLocation ADVENTURE_MESSAGE;
	public static ResourceLocation ELDER_MINION_COOLDOWN;
	public static ResourceLocation ELDER_MINION;
	public static String APPROACHING_ELDER_GUARDIAN = ProgressiveBosses.lang("elder_guardian.approach");
	@Config(description = "If true, the player will not be able to break blocks when an Elder Guardian is nearby. This also removes Mining Fatigue.")
	public static Boolean adventure = true;

	@Config(description = "The range from any Elder Guardian at which players get adventure mode. This range is doubled when YUNG's Better Ocean Monuments is installed.")
	public static Double adventureRange = 48d;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
        LEVEL = this.createDataKey("level");
        PREVIOUSLY_NEAR_ELDER_GUARDIAN = this.createDataKey("previously_near_elder_guardian");
        ADVENTURE_MESSAGE = this.createDataKey("adventure_message");
        ELDER_MINION_COOLDOWN = this.createDataKey("elder_minion_cooldown");
        ELDER_MINION = this.createDataKey("elder_minion");
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !adventure
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| (player.tickCount + player.getId()) % 20 != 0
				|| !player.isAlive())
			return;

		ServerLevel world = (ServerLevel) player.level();

		boolean previouslyNearElderGuardian = ModNBTData.get(player, PREVIOUSLY_NEAR_ELDER_GUARDIAN, Boolean.class);
		boolean adventureMessage = ModNBTData.get(player, ADVENTURE_MESSAGE, Boolean.class);

		float range = adventureRange.floatValue();
		if (ModList.get().isLoaded("betteroceanmonuments"))
			range *= 2f;
		boolean nearElderGuardian = !world.getEntitiesOfClass(ElderGuardian.class, player.getBoundingBox().inflate(range)).isEmpty();
        ModNBTData.put(player, PREVIOUSLY_NEAR_ELDER_GUARDIAN, nearElderGuardian);

		if (player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL && nearElderGuardian) {
			player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
			player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)GameType.ADVENTURE.getId()));
			if (!adventureMessage) {
				player.sendSystemMessage(Component.translatable(APPROACHING_ELDER_GUARDIAN));
				ModNBTData.put(player, ADVENTURE_MESSAGE, true);
			}
		}
		else if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && !nearElderGuardian && previouslyNearElderGuardian) {
			player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
			player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)GameType.SURVIVAL.getId()));
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !adventure
				|| !(event.getEntity() instanceof ServerPlayer serverPlayer))
			return;

		boolean previouslyNearElderGuardian = ModNBTData.get(serverPlayer, PREVIOUSLY_NEAR_ELDER_GUARDIAN, Boolean.class);

		if (previouslyNearElderGuardian && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
			serverPlayer.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
			serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)GameType.SURVIVAL.getId()));
		}
	}

	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Start event) {
		if (!this.isEnabled()
				|| !adventure
				|| event.getExplosion().getIndirectSourceEntity() == null
				|| event.getExplosion().getBlockInteraction() == Explosion.BlockInteraction.KEEP)
			return;

		boolean nearElderGuardian = !event.getLevel().getEntitiesOfClass(ElderGuardian.class, event.getExplosion().getIndirectSourceEntity().getBoundingBox().inflate(adventureRange)).isEmpty();
		if (nearElderGuardian) {
			event.setCanceled(true);
			event.getLevel().explode(event.getExplosion().getIndirectSourceEntity(), event.getExplosion().center().x, event.getExplosion().center().y, event.getExplosion().center().z, event.getExplosion().radius(), ((ExplosionAccessor) event.getExplosion()).isFire(), Level.ExplosionInteraction.NONE);
		}
	}

	@SubscribeEvent
	public void onElderGuardianDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		List<Entity> elderGuardiansNearby = elderGuardian.level().getEntities(elderGuardian, elderGuardian.getBoundingBox().inflate(64d), entity -> entity instanceof ElderGuardian);
		if (elderGuardiansNearby.isEmpty())
			return;

		int newLvl = getGuardianLvl(elderGuardian) + 1;

		elderGuardian.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2f, 0.5f);
		for (Entity elderGuardianNearby : elderGuardiansNearby) {
			ModNBTData.put(elderGuardianNearby, LEVEL, newLvl);
			updateHealth((ElderGuardian) elderGuardianNearby);
			updateExperienceDropped(elderGuardian);
		}
	}

    public static byte getGuardianLvl(ElderGuardian elderGuardian) {
        return ModNBTData.get(elderGuardian, LEVEL, Byte.class);
    }

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian)
                || elderGuardian.getSpawnType() == MobSpawnType.SPAWNER)
			return;

		if (ModNBTData.contains(elderGuardian, LEVEL))
			return;

		ModNBTData.put(elderGuardian, LEVEL, 0);
		Optional<ElderGuardianStats> oElderGuardianStats = getStats(elderGuardian);
		updateHealth(elderGuardian);
		updateExperienceDropped(elderGuardian);
		if (!ModNBTData.contains(elderGuardian, ELDER_MINION_COOLDOWN) && oElderGuardianStats.isPresent())
			ModNBTData.put(elderGuardian, ELDER_MINION_COOLDOWN, oElderGuardianStats.get().minionCooldown);
	}

	public static Optional<ElderGuardianStats> getStats(ElderGuardian elderGuardian) {
		int lvl = getGuardianLvl(elderGuardian);
		if (!ElderGuardianStatsReloadListener.STATS_MAP.containsKey(lvl))
			return Optional.empty();
		return Optional.of(ElderGuardianStatsReloadListener.STATS_MAP.get(lvl));
	}

	@SubscribeEvent
	public void onDamageDealt(LivingDamageEvent.Pre event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !(event.getSource().getEntity() instanceof ElderGuardian elderGuardian))
			return;
		Optional<ElderGuardianStats> oElderGuardianStats = ElderGuardianFeature.getStats(elderGuardian);
		if (oElderGuardianStats.isEmpty())
			return;

		event.setNewDamage(event.getNewDamage() * (1f + oElderGuardianStats.get().bonusDamage));
		if (!event.getSource().is(DamageTypes.THORNS))
			elderGuardian.heal(oElderGuardianStats.get().regenOnAttack / 2f);
	}

	/*
	 * Health
	 */
	private void updateHealth(ElderGuardian elderGuardian) {
		Optional<ElderGuardianStats> oElderGuardianStats = ElderGuardianFeature.getStats(elderGuardian);
		if (oElderGuardianStats.isEmpty())
			return;
		AttributeInstance maxHealth = elderGuardian.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth == null)
			return;
		double prevMaxHealth = maxHealth.getBaseValue();
		maxHealth.setBaseValue(oElderGuardianStats.get().health);
		elderGuardian.heal((float) (oElderGuardianStats.get().health - prevMaxHealth));

		if (oElderGuardianStats.get().absorption > 0)
			elderGuardian.setAbsorptionAmount(oElderGuardianStats.get().absorption);
	}

	/*
	 * Minions
	 */
	@SubscribeEvent
	public void update(EntityTickEvent.Post event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		Level world = event.getEntity().level();

		if (elderGuardian.getHealth() <= 0)
			return;
		int cooldown = ModNBTData.get(elderGuardian, ELDER_MINION_COOLDOWN, Integer.class);
		if (cooldown > 0) {
			ModNBTData.put(elderGuardian, ELDER_MINION_COOLDOWN, cooldown - 1);
			return;
		}
		Optional<ElderGuardianStats> oElderGuardianStats = getStats(elderGuardian);
		if (oElderGuardianStats.isEmpty())
			return;
		ModNBTData.put(elderGuardian, ELDER_MINION_COOLDOWN, oElderGuardianStats.get().minionCooldown);

		//If there is no player in a radius from the elderGuardian, don't spawn minions
		int radius = 24;
		BlockPos pos1 = elderGuardian.blockPosition().offset(-radius, -radius, -radius);
		BlockPos pos2 = elderGuardian.blockPosition().offset(radius, radius, radius);
		AABB bb = AABB.encapsulatingFullBlocks(pos1, pos2);
		List<ServerPlayer> players = world.getEntitiesOfClass(ServerPlayer.class, bb);

		if (players.isEmpty())
			return;

		List<Guardian> guardiansInRange = world.getEntitiesOfClass(Guardian.class, elderGuardian.getBoundingBox().inflate(12));

		if (guardiansInRange.size() >= 6)
			return;

		summonMinion(world, new Vec3(elderGuardian.getX(), elderGuardian.getY(), elderGuardian.getZ()));
	}

	public static void summonMinion(Level world, Vec3 pos) {
		Guardian elderMinion = new Guardian(EntityType.GUARDIAN, world);
        //TODO Fix
		//minionTags.putBoolean("mobspropertiesrandomness:processed", true);

		ModNBTData.put(elderMinion, ElderGuardianFeature.ELDER_MINION, true);

		elderMinion.setPos(pos.x, pos.y, pos.z);
		elderMinion.setCustomName(Component.translatable(Util.makeDescriptionId("entity", ELDER_MINION)));
		((MobAccessor) elderMinion).setLootTable(BuiltInLootTables.EMPTY);

		MCUtils.applyModifier(elderMinion, NeoForgeMod.SWIM_SPEED, ELDER_MINION_BONUS, 2d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		MCUtils.applyModifier(elderMinion, Attributes.MAX_HEALTH, ELDER_MINION_BONUS, -0.5d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : elderMinion.targetSelector.getAvailableGoals()) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal))
				continue;

			goalsToRemove.add(prioritizedGoal.getGoal());
		}

		goalsToRemove.forEach(elderMinion.goalSelector::removeGoal);
		elderMinion.targetSelector.addGoal(1, new ILNearestAttackableTargetGoal<>(elderMinion, Player.class, false).setIgnoreLineOfSight());

		world.addFreshEntity(elderMinion);
	}

	/*
	 * Experience
	 */
	public static void updateExperienceDropped(ElderGuardian elderGuardian) {
		Optional<ElderGuardianStats> oElderGuardianStats = ElderGuardianFeature.getStats(elderGuardian);
		if (oElderGuardianStats.isEmpty())
			return;
		((MobAccessor) elderGuardian).setXpReward(oElderGuardianStats.get().xpDropped);
	}

	/*
	 * Resistances
	 */
	@SubscribeEvent
	public void onElderGuardianDamage(LivingDamageEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		Optional<ElderGuardianStats> oElderGuardianStats = ElderGuardianFeature.getStats(elderGuardian);
		if (oElderGuardianStats.isEmpty())
			return;
		float damageReduction = oElderGuardianStats.get().damageResistance;

		event.setNewDamage(event.getNewDamage() * (1f - damageReduction));
	}
}
