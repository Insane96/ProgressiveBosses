package insane96mcp.progressivebosses.module.elderguardian;

import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.elderguardian.data.ElderGuardianStats;
import insane96mcp.progressivebosses.module.elderguardian.data.ElderGuardianStatsReloadListener;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Label(name = "Base", description = "Base feature for the Elder Guardian harder fights.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian", canBeDisabled = false)
public class ElderGuardianFeature extends Feature {
	public static final String LVL = ProgressiveBosses.RESOURCE_PREFIX + "level";
	public static final String PREVIOUSLY_NEAR_ELDER_GUARDIAN = ProgressiveBosses.RESOURCE_PREFIX + "previously_near_elder_guardian";
	public static final String ADVENTURE_MESSAGE = ProgressiveBosses.RESOURCE_PREFIX + "adventure_message";
	public static final String ELDER_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "elder_minion_cooldown";
	public static final String ELDER_MINION = ProgressiveBosses.RESOURCE_PREFIX + "elder_minion";
	@Config
	@Label(name = "Adventure mode", description = "If true, the player will not be able to break blocks when an Elder Guardian is nearby. This also removes Mining Fatigue.")
	public static Boolean adventure = true;

	@Config
	@Label(name = "Adventure mode Range", description = "The range from any Elder Guardian at which players get adventure mode. This range is doubled when YUNG's Better Ocean Monuments is installed.")
	public static Double adventureRange = 48d;

	public ElderGuardianFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player.level().isClientSide
				|| !this.isEnabled()
				|| !adventure
				|| event.player.tickCount % 20 != 0
				|| !event.player.isAlive())
			return;

		ServerPlayer serverPlayer = (ServerPlayer) event.player;
		ServerLevel world = (ServerLevel) serverPlayer.level();

		CompoundTag nbt = serverPlayer.getPersistentData();
		boolean previouslyNearElderGuardian = nbt.getBoolean(PREVIOUSLY_NEAR_ELDER_GUARDIAN);
		boolean adventureMessage = nbt.getBoolean(ADVENTURE_MESSAGE);

		float range = adventureRange.floatValue();
		if (ModList.get().isLoaded("betteroceanmonuments"))
			range *= 2f;
		boolean nearElderGuardian = !world.getEntitiesOfClass(ElderGuardian.class, serverPlayer.getBoundingBox().inflate(range)).isEmpty();
		nbt.putBoolean(PREVIOUSLY_NEAR_ELDER_GUARDIAN, nearElderGuardian);

		if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL && nearElderGuardian) {
			serverPlayer.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
			serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)GameType.ADVENTURE.getId()));
			if (!adventureMessage) {
				serverPlayer.sendSystemMessage(Component.translatable(Strings.Translatable.APPROACHING_ELDER_GUARDIAN));
				nbt.putBoolean(ADVENTURE_MESSAGE, true);
			}
		}
		else if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && !nearElderGuardian && previouslyNearElderGuardian) {
			serverPlayer.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
			serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)GameType.SURVIVAL.getId()));
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !adventure
				|| !(event.getEntity() instanceof ServerPlayer serverPlayer))
			return;

		CompoundTag nbt = serverPlayer.getPersistentData();
		boolean previouslyNearElderGuardian = nbt.getBoolean(PREVIOUSLY_NEAR_ELDER_GUARDIAN);

		if (previouslyNearElderGuardian && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
			serverPlayer.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
			serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)GameType.SURVIVAL.getId()));
		}
	}

	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Start event) {
		if (!this.isEnabled()
				|| !adventure
				|| event.getExplosion().getExploder() == null
				|| event.getExplosion().blockInteraction == Explosion.BlockInteraction.KEEP)
			return;

		boolean nearElderGuardian = !event.getLevel().getEntitiesOfClass(ElderGuardian.class, event.getExplosion().getExploder().getBoundingBox().inflate(adventureRange)).isEmpty();
		if (nearElderGuardian) {
			event.setCanceled(true);
			event.getLevel().explode(event.getExplosion().getExploder(), event.getExplosion().getPosition().x, event.getExplosion().getPosition().y, event.getExplosion().getPosition().z, event.getExplosion().radius, event.getExplosion().fire, Level.ExplosionInteraction.NONE);
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

		CompoundTag nbt = elderGuardian.getPersistentData();
		int newLvl = nbt.getInt(LVL) + 1;

		elderGuardian.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2f, 0.5f);
		for (Entity elderGuardianNearby : elderGuardiansNearby) {
			elderGuardianNearby.getPersistentData().putInt(LVL, newLvl);
			updateHealth((ElderGuardian) elderGuardianNearby);
			updateExperienceDropped(elderGuardian);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		CompoundTag nbt = elderGuardian.getPersistentData();
		if (nbt.contains(LVL))
			return;

		nbt.putInt(LVL, 0);
		Optional<ElderGuardianStats> oElderGuardianStats = getStats(elderGuardian);
		updateHealth(elderGuardian);
		updateExperienceDropped(elderGuardian);
		if (!nbt.contains(ELDER_MINION_COOLDOWN) && oElderGuardianStats.isPresent())
			nbt.putInt(ELDER_MINION_COOLDOWN, oElderGuardianStats.get().minionCooldown);
	}

	public static Optional<ElderGuardianStats> getStats(ElderGuardian elderGuardian) {
		int lvl = elderGuardian.getPersistentData().getInt(LVL);
		if (!ElderGuardianStatsReloadListener.STATS_MAP.containsKey(lvl))
			return Optional.empty();
		return Optional.of(ElderGuardianStatsReloadListener.STATS_MAP.get(lvl));
	}

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !(event.getSource().getEntity() instanceof ElderGuardian elderGuardian))
			return;
		Optional<ElderGuardianStats> oElderGuardianStats = ElderGuardianFeature.getStats(elderGuardian);
		if (oElderGuardianStats.isEmpty())
			return;

		event.setAmount(event.getAmount() * (1f + oElderGuardianStats.get().bonusDamage));
		if (!event.getSource().is(DamageTypes.THORNS))
			elderGuardian.heal(oElderGuardianStats.get().regenOnAttack / 2f);
	}

	public static int getAttackDuration(ElderGuardian elderGuardian) {
		Optional<ElderGuardianStats> oElderGuardianStats = ElderGuardianFeature.getStats(elderGuardian);
		return oElderGuardianStats.map(elderGuardianStats -> elderGuardianStats.attackDuration).orElse(60 /*Vanilla attack duration*/);
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
	public void update(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		Level world = event.getEntity().level();

		CompoundTag elderGuardianTags = elderGuardian.getPersistentData();

		if (elderGuardian.getHealth() <= 0)
			return;
		int cooldown = elderGuardianTags.getInt(ELDER_MINION_COOLDOWN);
		if (cooldown > 0) {
			elderGuardianTags.putInt(ELDER_MINION_COOLDOWN, cooldown - 1);
			return;
		}
		Optional<ElderGuardianStats> oElderGuardianStats = getStats(elderGuardian);
		if (oElderGuardianStats.isEmpty())
			return;
		elderGuardianTags.putInt(ELDER_MINION_COOLDOWN, oElderGuardianStats.get().minionCooldown);

		//If there is no player in a radius from the elderGuardian, don't spawn minions
		int radius = 24;
		BlockPos pos1 = elderGuardian.blockPosition().offset(-radius, -radius, -radius);
		BlockPos pos2 = elderGuardian.blockPosition().offset(radius, radius, radius);
		AABB bb = new AABB(pos1, pos2);
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
		CompoundTag minionTags = elderMinion.getPersistentData();

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);

		minionTags.putBoolean(ElderGuardianFeature.ELDER_MINION, true);

		elderMinion.setPos(pos.x, pos.y, pos.z);
		elderMinion.setCustomName(Component.translatable(Strings.Translatable.ELDER_MINION));
		elderMinion.lootTable = BuiltInLootTables.EMPTY;

		MCUtils.applyModifier(elderMinion, ForgeMod.SWIM_SPEED.get(), Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2d, AttributeModifier.Operation.MULTIPLY_BASE);
		MCUtils.applyModifier(elderMinion, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, -0.5d, AttributeModifier.Operation.MULTIPLY_BASE);

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : elderMinion.targetSelector.availableGoals) {
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
		elderGuardian.xpReward = oElderGuardianStats.get().xpDropped;
	}

	/*
	 * Resistances
	 */
	@SubscribeEvent
	public void onElderGuardianDamage(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		Optional<ElderGuardianStats> oElderGuardianStats = ElderGuardianFeature.getStats(elderGuardian);
		if (oElderGuardianStats.isEmpty())
			return;
		float damageReduction = oElderGuardianStats.get().damageResistance;

		event.setAmount(event.getAmount() * (1f - damageReduction));
	}
}
