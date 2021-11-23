package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.monster.ElderGuardianEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Base", description = "Base feature for the Elder Guardian harder fights. Disabling this feature will disable the added sound when an Elder Guardian is killed.")
public class BaseFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> adventureConfig;

	public boolean adventure = true;

	public BaseFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		this.adventureConfig = Config.builder
				.comment("If true, the player will not be able to break blocks when an Elder Guardian is nearby.")
				.define("Adventure mode", this.adventure);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.adventure = this.adventureConfig.get();
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player.level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!this.adventure)
			return;

		if (event.player.tickCount % 20 != 0)
			return;

		if (!event.player.isAlive())
			return;

		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.player;
		ServerWorld world = (ServerWorld) serverPlayer.level;

		CompoundNBT nbt = serverPlayer.getPersistentData();
		boolean previouslyNearElderGuardian = nbt.getBoolean(Strings.Tags.PREVIOUSLY_NEAR_ELDER_GUARDIAN);
		boolean adventureMessage = nbt.getBoolean(Strings.Tags.ADVENTURE_MESSAGE);

		boolean nearElderGuardian = !world.getEntitiesOfClass(ElderGuardianEntity.class, serverPlayer.getBoundingBox().inflate(32d), null).isEmpty();
		nbt.putBoolean(Strings.Tags.PREVIOUSLY_NEAR_ELDER_GUARDIAN, nearElderGuardian);

		if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL && nearElderGuardian) {
			serverPlayer.gameMode.setGameModeForPlayer(GameType.ADVENTURE);
			serverPlayer.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.CHANGE_GAME_MODE, (float)GameType.ADVENTURE.getId()));
			if (!adventureMessage) {
				serverPlayer.sendMessage(new TranslationTextComponent(Strings.Translatable.APPROACHING_ELDER_GUARDIAN), Util.NIL_UUID);
				nbt.putBoolean(Strings.Tags.ADVENTURE_MESSAGE, true);
			}
		}
		else if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && !nearElderGuardian && previouslyNearElderGuardian) {
			serverPlayer.gameMode.setGameModeForPlayer(GameType.SURVIVAL);
			serverPlayer.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.CHANGE_GAME_MODE, (float)GameType.SURVIVAL.getId()));
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled())
			return;

		if (!this.adventure)
			return;

		if (!(event.getEntity() instanceof ServerPlayerEntity))
			return;

		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.getEntity();

		CompoundNBT nbt = serverPlayer.getPersistentData();
		boolean previouslyNearElderGuardian = nbt.getBoolean(Strings.Tags.PREVIOUSLY_NEAR_ELDER_GUARDIAN);

		if (previouslyNearElderGuardian && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
			serverPlayer.gameMode.setGameModeForPlayer(GameType.SURVIVAL);
			serverPlayer.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.CHANGE_GAME_MODE, (float)GameType.SURVIVAL.getId()));
		}
	}

	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Start event) {
		if (!this.isEnabled())
			return;

		if (!this.adventure)
			return;

		if (event.getExplosion().getExploder() == null)
			return;

		boolean nearElderGuardian = !event.getWorld().getEntitiesOfClass(ElderGuardianEntity.class, event.getExplosion().getExploder().getBoundingBox().inflate(32d)).isEmpty();
		if (nearElderGuardian)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onElderGuardianDeath(LivingDeathEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ElderGuardianEntity))
			return;

		ElderGuardianEntity elderGuardian = (ElderGuardianEntity) event.getEntity();

		int elderGuardiansNearby = elderGuardian.level.getEntities(elderGuardian, elderGuardian.getBoundingBox().inflate(48d), entity -> entity instanceof ElderGuardianEntity).size();
		if (elderGuardiansNearby == 0)
			return;

		elderGuardian.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2f, 0.5f);
	}

	public static int getDeadElderGuardians(ElderGuardianEntity elderGuardian) {
		int elderGuardiansNearby = elderGuardian.level.getEntities(elderGuardian, elderGuardian.getBoundingBox().inflate(48d), entity -> entity instanceof ElderGuardianEntity).size();
		return 2 - elderGuardiansNearby;
	}
}
