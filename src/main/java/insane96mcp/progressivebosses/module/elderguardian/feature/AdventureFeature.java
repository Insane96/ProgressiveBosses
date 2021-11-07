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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Adventure", description = "Player's not able to break blocks while in range of Elder Guardians.")
public class AdventureFeature extends Feature {

	//private final ForgeConfigSpec.ConfigValue<Double> bonusHealthConfig;

	//public double bonusHealth = 0d;

	public AdventureFeature(Module module) {
		super(Config.builder, module);
		/*this.pushConfig(Config.builder);
		this.bonusHealthConfig = Config.builder
				.comment("Increase Elder Guardians' Health by this percentage (1 = +100% health)")
				.defineInRange("Health Bonus per Difficulty", this.bonusHealth, 0.0, Double.MAX_VALUE);
		Config.builder.pop();*/
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		//this.bonusHealth = this.bonusHealthConfig.get();
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player.world.isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (event.player.ticksExisted % 20 != 0)
			return;

		if (!event.player.isAlive())
			return;

		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.player;
		ServerWorld world = (ServerWorld) serverPlayer.world;

		CompoundNBT nbt = serverPlayer.getPersistentData();
		boolean previouslyNearElderGuardian = nbt.getBoolean(Strings.Tags.PREVIOUSLY_NEAR_ELDER_GUARDIAN);
		boolean adventureMessage = nbt.getBoolean(Strings.Tags.ADVENTURE_MESSAGE);

		boolean nearElderGuardian = !world.getEntitiesWithinAABB(ElderGuardianEntity.class, serverPlayer.getBoundingBox().grow(32d), null).isEmpty();
		nbt.putBoolean(Strings.Tags.PREVIOUSLY_NEAR_ELDER_GUARDIAN, nearElderGuardian);

		if (serverPlayer.interactionManager.getGameType() == GameType.SURVIVAL && nearElderGuardian) {
			serverPlayer.interactionManager.setGameType(GameType.ADVENTURE);
			serverPlayer.connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.CHANGE_GAMETYPE, (float)GameType.ADVENTURE.getID()));
			if (!adventureMessage) {
				serverPlayer.sendMessage(new TranslationTextComponent(Strings.Translatable.APPROACHING_ELDER_GUARDIAN), Util.DUMMY_UUID);
				nbt.putBoolean(Strings.Tags.ADVENTURE_MESSAGE, true);
			}
		}
		else if (serverPlayer.interactionManager.getGameType() == GameType.ADVENTURE && !nearElderGuardian && previouslyNearElderGuardian) {
			serverPlayer.interactionManager.setGameType(GameType.SURVIVAL);
			serverPlayer.connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.CHANGE_GAMETYPE, (float)GameType.SURVIVAL.getID()));
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ServerPlayerEntity))
			return;

		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.getEntity();

		CompoundNBT nbt = serverPlayer.getPersistentData();
		boolean previouslyNearElderGuardian = nbt.getBoolean(Strings.Tags.PREVIOUSLY_NEAR_ELDER_GUARDIAN);

		if (previouslyNearElderGuardian && serverPlayer.interactionManager.getGameType() == GameType.ADVENTURE) {
			serverPlayer.interactionManager.setGameType(GameType.SURVIVAL);
			serverPlayer.connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.CHANGE_GAMETYPE, (float)GameType.SURVIVAL.getID()));
		}
	}

	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Start event) {
		if (!this.isEnabled())
			return;

		if (event.getExplosion().getExploder() == null)
			return;

		boolean nearElderGuardian = !event.getWorld().getEntitiesWithinAABB(ElderGuardianEntity.class, event.getExplosion().getExploder().getBoundingBox().grow(32d)).isEmpty();
		if (nearElderGuardian)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onElderGuardianDeath(LivingDeathEvent event) {
		//if (!this.isEnabled())
			//return;

		if (!(event.getEntity() instanceof ElderGuardianEntity))
			return;

		ElderGuardianEntity elderGuardian = (ElderGuardianEntity) event.getEntity();

		int elderGuardiansNearby = elderGuardian.world.getEntitiesInAABBexcluding(elderGuardian, elderGuardian.getBoundingBox().grow(48d), entity -> entity instanceof ElderGuardianEntity).size();
		if (elderGuardiansNearby == 0)
			return;

		elderGuardian.playSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 2f, 0.5f);
	}
}
