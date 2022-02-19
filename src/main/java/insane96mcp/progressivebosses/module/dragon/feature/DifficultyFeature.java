package insane96mcp.progressivebosses.module.dragon.feature;

import com.google.common.util.concurrent.AtomicDouble;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.progressivebosses.capability.Difficulty;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Label(name = "Difficulty Settings", description = "How difficulty is handled for the Dragon.")
public class DifficultyFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> sumKilledDragonDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusDifficultyPerPlayerConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> startingDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> showFirstKilledDragonMessageConfig;

	public boolean sumKilledDragonDifficulty = false;
	public double bonusDifficultyPerPlayer = 0.25d;
	public int maxDifficulty = 24;
	public int startingDifficulty = 0;
	public boolean showFirstKilledDragonMessage = true;

	public DifficultyFeature(Module module) {
		super(Config.builder, module, true, false);
		this.pushConfig(Config.builder);
		sumKilledDragonDifficultyConfig = Config.builder
				.comment("If false and there's more than 1 player around the Dragon, difficulty will be the average of all the players' difficulty instead of summing them.")
				.define("Sum Killed Dragons Difficulty", sumKilledDragonDifficulty);
		bonusDifficultyPerPlayerConfig = Config.builder
				.comment("Percentage bonus difficulty added to the Dragon when more than one player is present. Each player past the first one will add this percentage to the difficulty.")
				.defineInRange("Bonus Difficulty per Player", this.bonusDifficultyPerPlayer, 0d, 24d);
		maxDifficultyConfig = Config.builder
				.comment("The Maximum difficulty (times killed) reachable by Ender Dragon. By default is set to 24 because it's the last spawning end gate.")
				.defineInRange("Max Difficulty", maxDifficulty, 1, Integer.MAX_VALUE);
		startingDifficultyConfig = Config.builder
				.comment("How much difficulty will players start with when joining a world? Note that this will apply when the player joins the world if the current player difficulty is below this value.")
				.defineInRange("Starting Difficulty", startingDifficulty, 0, Integer.MAX_VALUE);
		this.showFirstKilledDragonMessageConfig = Config.builder
				.comment("Set to false to disable the first Dragon killed message.")
				.define("Show First Killed Dragon Message", this.showFirstKilledDragonMessage);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.sumKilledDragonDifficulty = this.sumKilledDragonDifficultyConfig.get();
		this.bonusDifficultyPerPlayer = this.bonusDifficultyPerPlayerConfig.get();
		this.maxDifficulty = this.maxDifficultyConfig.get();
		this.startingDifficulty = this.startingDifficultyConfig.get();
		this.showFirstKilledDragonMessage = this.showFirstKilledDragonMessageConfig.get();
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!event.getWorld().dimension().location().equals(DimensionType.END_LOCATION.location()))
			return;

		if (!(event.getEntity() instanceof EnderDragon))
			return;

		EnderDragon dragon = (EnderDragon) event.getEntity();

		if (dragon.getDragonFight() == null)
			return;

		CompoundTag dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.DIFFICULTY))
			return;

		int radius = 256;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AABB bb = new AABB(pos1, pos2);

		List<ServerPlayer> players = event.getWorld().getEntitiesOfClass(ServerPlayer.class, bb);

		if (players.size() == 0)
			return;

		AtomicInteger playersFirstDragon = new AtomicInteger(0);
		final AtomicDouble dragonDifficulty = new AtomicDouble(0d);

		for (ServerPlayer player : players) {
			player.getCapability(Difficulty.INSTANCE).ifPresent(difficulty -> {
				dragonDifficulty.addAndGet(difficulty.getKilledDragons());
				if (difficulty.getFirstDragon() == (byte) 1) {
					playersFirstDragon.incrementAndGet();
					difficulty.setFirstDragon((byte) 2);
				}
			});
		}

		dragonTags.putInt(Strings.Tags.EGGS_TO_DROP, playersFirstDragon.get());

		if (!this.sumKilledDragonDifficulty)
			dragonDifficulty.set(dragonDifficulty.get() / players.size());

		if (players.size() > 1)
			dragonDifficulty.set(dragonDifficulty.get() * (1d + ((players.size() - 1) * this.bonusDifficultyPerPlayer)));

		dragonTags.putFloat(Strings.Tags.DIFFICULTY, (float) dragonDifficulty.get());
	}

	//Increase Player Difficulty
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon))
			return;

		EnderDragon dragon = (EnderDragon) event.getEntity();

		int radius = 256;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AABB bb = new AABB(pos1, pos2);

		List<ServerPlayer> players = dragon.level.getEntitiesOfClass(ServerPlayer.class, bb);
		//If no players are found in the "Spawn Radius Player Check", try to get the nearest player
		if (players.size() == 0) {
			ServerPlayer nearestPlayer = (ServerPlayer) dragon.level.getNearestPlayer(dragon.getX(), dragon.getY(), dragon.getZ(), Double.MAX_VALUE, true);
			players.add(nearestPlayer);
		}

		for (ServerPlayer player : players) {
			player.getCapability(Difficulty.INSTANCE).ifPresent(difficulty -> {
				if (difficulty.getKilledDragons() <= this.startingDifficulty && this.showFirstKilledDragonMessage)
					player.sendMessage(new TranslatableComponent(Strings.Translatable.FIRST_DRAGON_KILL), Util.NIL_UUID);
				if (difficulty.getKilledDragons() < this.maxDifficulty)
					difficulty.addKilledDragons(1);
			});
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void setPlayerData(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		player.getCapability(Difficulty.INSTANCE).ifPresent(difficulty -> {
			if (difficulty.getKilledDragons() < this.startingDifficulty) {
				difficulty.setKilledDragons(this.startingDifficulty);
				LogHelper.info("[Progressive Bosses] %s killed dragons counter was below the set 'Starting Difficulty', Has been increased to match 'Starting Difficulty'", player.getName().getString());
			}
			if (difficulty.getKilledDragons() > this.maxDifficulty) {
				difficulty.setKilledDragons(this.maxDifficulty);
				LogHelper.info("[Progressive Bosses] %s killed dragons counter was above the 'Max Difficulty', Has been decreased to match 'Max Difficulty'", player.getName().getString());
			}

			if (difficulty.getFirstDragon() == 0) {
				difficulty.setFirstDragon((byte) 1);
				LogHelper.info("[Progressive Bosses] %s first spawned. Set First Dragon to 1", player.getName().getString());
			}
		});
	}
}
