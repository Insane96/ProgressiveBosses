package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.network.MessageWitherSync;
import insane96mcp.progressivebosses.network.PacketManager;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

@Label(name = "Attack", description = "Makes the Wither smarter (will no longer try to stand on the player's head ...), attack faster and hit harder")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class AttackFeature extends Feature {
	@Config(min = 0d, max = 1d)
	@Label(name = "Charge attack.Chance", description = "Chance every time the Wither takes damage to start a charge attack. Lower health and more damage taken increases the chance.\n" +
			"This value is the chance at 0% health and when taking 10 damage.")
	public static Double maxChargeAttackChance = 0.05d;
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
	public static Integer maxBarrageDuration = 140;

	public AttackFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| !event.getEntity().isAlive()
				|| !(event.getEntity() instanceof PBWither wither))
			return;

		tickCharge(wither);

		if (event.getEntity().level().isClientSide)
			return;

		chargeUnseen(wither);
	}

	private void tickCharge(PBWither wither) {
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

	private void chargeUnseen(PBWither wither) {
		CompoundTag witherTags = wither.getPersistentData();

		if (witherTags.getByte(Strings.Tags.CHARGE_ATTACK) <= 0 && wither.tickCount % 20 == 0) {
			//doCharge(wither, witherTags.getInt(PBWither.UNSEEN_PLAYER_TICKS) / 20f);
		}
	}

	//High-priority so runs before the damage reduction
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDamaged(LivingHurtEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !event.getEntity().isAlive()
				|| !(event.getEntity() instanceof PBWither wither))
			return;

		doBarrage(wither, event.getAmount());
		doCharge(wither, event.getAmount());
	}

	private void doBarrage(PBWither wither, float damageTaken) {
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

	private void doCharge(PBWither wither, float damageTaken) {
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

	public static class Consts {
		public static final int CHARGE_ATTACK_TICK_START = 90;
		public static final int CHARGE_ATTACK_TICK_CHARGE = 30;
	}

	public static void initCharging(PBWither wither) {
		wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, (byte) Consts.CHARGE_ATTACK_TICK_START);
		Object msg = new MessageWitherSync(wither.getId(), (byte) Consts.CHARGE_ATTACK_TICK_START);
		for (Player player : wither.level().players()) {
			PacketManager.CHANNEL.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public static void stopCharging(PBWither wither) {
		wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, (byte) 0);
		Object msg = new MessageWitherSync(wither.getId(), (byte) 0);
		for (Player player : wither.level().players()) {
			PacketManager.CHANNEL.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}
}
