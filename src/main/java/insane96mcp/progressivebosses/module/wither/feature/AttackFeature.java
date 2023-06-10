package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.ai.WitherChargeAttackGoal;
import insane96mcp.progressivebosses.module.wither.ai.WitherRangedAttackGoal;
import insane96mcp.progressivebosses.network.MessageWitherSync;
import insane96mcp.progressivebosses.network.PacketManager;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.UUID;

@Label(name = "Attack", description = "Makes the Wither smarter (will no longer try to stand on the player's head ...), attack faster and hit harder")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class AttackFeature extends Feature {
	@Config(min = 0d)
	@Label(name = "Increased Damage", description = "How much experience will an Elder Guardian drop. -1 will make the Elder Guardian drop vanilla experience.")
	public static Double increasedDamage = 0.96d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Charge attack.Chance", description = "Chance every time the Wither takes damage to start a charge attack. Lower health and more damage taken increases the chance.\n" +
			"This value is the chance at 0% health and when taking 10 damage.")
	public static Double maxChargeAttackChance = 0.06d;
	@Config(min = 0d, max = 32d)
	@Label(name = "Charge attack.Base Damage", description = "Base damage of the charge attack. Increased by 'Increased Damage' config option.")
	public static Double chargeAttackBaseDamage = 12d;
	//Barrage Attack
	@Config(min = 0d, max = 1d)
	@Label(name = "Barrage Attack.Chance", description = "Chance (at max difficulty) every time the Wither takes damage to start a barrage attack. More damage taken increases the chance.\n" +
			"This value is the chance when taking 10 damage.")
	public static Double maxBarrageChance = 0.075d;
	@Config(min = 0)
	@Label(name = "Barrage Attack.Min Duration", description = "Min time (in ticks) for the duration of the barrage attack. Less health = longer barrage.")
	public static Integer minBarrageDuration = 20;
	@Config(min = 0)
	@Label(name = "Barrage Attack.Max Duration", description = "Max time (in ticks) for the duration of the barrage attack. Less health = longer barrage")
	public static Integer maxBarrageDuration = 150;
	@Config(min = 1d)
	@Label(name = "Skull Velocity Multiplier", description = "Wither Skull Projectiles speed will be multiplied by this value. Set to 1 to not change the speed.")
	//Skulls
	public static Double skullVelocityMultiplier = 2.5d;
	//Attack Speed
	@Config(min = 1)
	@Label(name = "Attack Speed.Interval", description = "Every how many ticks (20 ticks = 1 seconds) the middle head will fire a projectile to the target.")
	public static Integer attackInterval = 35;
	@Config(min = 0d, max = 1d)
	@Label(name = "Attack Speed.Bonus when near", description = "The middle head will attack faster (up to this bonus percentage) the nearer the target is to the Wither.")
	public static Double bonusAttackSpeedWhenNear = 0.6d;

	public AttackFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		witherSkullSpeed(event.getEntity());
		setWitherAI(event.getEntity());
	}

	//TODO Scale skulls speed with difficulty (requires client sync)
	private void witherSkullSpeed(Entity entity) {
		if (!(entity instanceof WitherSkull witherSkullEntity)
				|| !this.isEnabled())
			return;

		//Prevent ultra-fast wither skulls from breaking the game
		if (Math.abs(witherSkullEntity.xPower) > 10 || Math.abs(witherSkullEntity.yPower) > 10 || Math.abs(witherSkullEntity.zPower) > 10) {
			entity.kill();
			return;
		}

		witherSkullEntity.xPower *= skullVelocityMultiplier;
		witherSkullEntity.yPower *= skullVelocityMultiplier;
		witherSkullEntity.zPower *= skullVelocityMultiplier;
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| !event.getEntity().isAlive()
				|| !(event.getEntity() instanceof WitherBoss wither))
			return;

		tickCharge(wither);

		if (event.getEntity().level.isClientSide)
			return;

		chargeUnseen(wither);
	}

	private void tickCharge(WitherBoss wither) {
		if (maxChargeAttackChance == 0d)
			return;
		byte chargeTick = wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK);

		// When in charge attack remove the vanilla health regeneration when he's invulnerable and add 1% health regeneration of the missing health per second
		if (chargeTick > 0){
			if (wither.tickCount % 10 == 0) {
				float missingHealth = wither.getMaxHealth() - wither.getHealth();
				wither.setHealth(wither.getHealth() + (missingHealth * 0.005f));
			}
			wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, (byte) (chargeTick - 1));
		}
	}

	private void chargeUnseen(WitherBoss wither) {
		CompoundTag witherTags = wither.getPersistentData();

		if (witherTags.getByte(Strings.Tags.CHARGE_ATTACK) <= 0 && wither.tickCount % 20 == 0) {
			doCharge(wither, witherTags.getInt(Strings.Tags.UNSEEN_PLAYER_TICKS) / 20f);
		}
	}

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| increasedDamage == 0d
				|| !(event.getSource().getEntity() instanceof WitherBoss wither))
			return;

		event.setAmount(event.getAmount() * (float)(1d + (increasedDamage * DifficultyHelper.getScalingDifficulty(wither))));
	}

	//High priority so runs before the damage reduction
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDamaged(LivingHurtEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| !event.getEntity().isAlive()
				|| !(event.getEntity() instanceof WitherBoss wither))
			return;

		doBarrage(wither, event.getAmount());
		doCharge(wither, event.getAmount());
	}

	private void doBarrage(WitherBoss wither, float damageTaken) {
		if (maxBarrageChance == 0d)
			return;

		CompoundTag witherTags = wither.getPersistentData();
		double missingHealthPerc = 1d - wither.getHealth() / wither.getMaxHealth();

		double chance = (maxBarrageChance * DifficultyHelper.getScalingDifficulty(wither));
		chance *= (damageTaken / 10f);
		double r = wither.getRandom().nextDouble();
		if (r < chance) {
			int duration = (int) (((maxBarrageDuration - minBarrageDuration) * missingHealthPerc) + minBarrageDuration);
			witherTags.putInt(Strings.Tags.BARRAGE_ATTACK, duration);
		}
	}

	private void doCharge(WitherBoss wither, float damageTaken) {
		if (maxChargeAttackChance == 0d
				|| wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK) > 0)
			return;

		double missingHealthPerc = 1d - wither.getHealth() / wither.getMaxHealth();
		double chance = maxChargeAttackChance * missingHealthPerc;
		chance *= (damageTaken / 10f);
		double r = wither.getRandom().nextDouble();
		if (r < chance) {
			initCharging(wither);
		}
	}

	public void setWitherAI(Entity entity) {
		if (entity.level.isClientSide
				|| !this.isEnabled()
				|| !(entity instanceof WitherBoss wither))
			return;

		ArrayList<Goal> toRemove = new ArrayList<>();
		wither.goalSelector.availableGoals.forEach(goal -> {
			if (goal.getGoal() instanceof RangedAttackGoal)
				toRemove.add(goal.getGoal());
			if (goal.getGoal() instanceof WitherBoss.WitherDoNothingGoal)
				toRemove.add(goal.getGoal());
		});

		toRemove.forEach(wither.goalSelector::removeGoal);

		wither.goalSelector.addGoal(1, new WitherChargeAttackGoal(wither));
		wither.goalSelector.addGoal(2, new WitherRangedAttackGoal(wither,  attackInterval, 24.0f, bonusAttackSpeedWhenNear));

		MCUtils.applyModifier(wither, Attributes.FOLLOW_RANGE, UUID.randomUUID(), "Wither Sexy Glasses", 56d, AttributeModifier.Operation.ADDITION);
	}

	public static class Consts {
		public static final int CHARGE_ATTACK_TICK_START = 90;
		public static final int CHARGE_ATTACK_TICK_CHARGE = 30;
	}

	public static void initCharging(WitherBoss wither) {
		wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, (byte) Consts.CHARGE_ATTACK_TICK_START);
		Object msg = new MessageWitherSync(wither.getId(), (byte) Consts.CHARGE_ATTACK_TICK_START);
		for (Player player : wither.level.players()) {
			PacketManager.CHANNEL.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public static void stopCharging(WitherBoss wither) {
		wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, (byte) 0);
		Object msg = new MessageWitherSync(wither.getId(), (byte) 0);
		for (Player player : wither.level.players()) {
			PacketManager.CHANNEL.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}
}
