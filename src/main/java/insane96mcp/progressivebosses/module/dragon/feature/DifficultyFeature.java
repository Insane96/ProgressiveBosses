package insane96mcp.progressivebosses.module.dragon.feature;

import com.google.common.util.concurrent.AtomicDouble;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.LogHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.capability.Difficulty;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Label(name = "Difficulty Settings", description = "How difficulty is handled for the Dragon.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon", canBeDisabled = false)
public class DifficultyFeature extends Feature {

	@Config(min = 1)
	@Label(name = "Max Difficulty", description = "The Maximum difficulty (times killed) reachable by Ender Dragon. By default is set to 24 because it's the last spawning end gate.")
	public static Integer maxDifficulty = 8;
	@Config(min = 0)
	@Label(name = "Starting Difficulty", description = "How much difficulty will players start with when joining a world? Note that this will apply when the player joins the world if the current player difficulty is below this value.")
	public static Integer startingDifficulty = 0;
	@Config
	@Label(name = "Sum Killed Dragons Difficulty", description = "If false and there's more than 1 player around the Dragon, difficulty will be the average of all the players' difficulty instead of summing them.")
	public static Boolean sumKilledDragonDifficulty = false;

	@Config(min = 0d)
	@Label(name = "Bonus Difficulty per Player", description = "Percentage bonus difficulty added to the Dragon when more than one player is present. Each player past the first one will add this percentage to the difficulty.")
	public static Double bonusDifficultyPerPlayer = 0.25d;
	@Config
	@Label(name = "Show First Killed Dragon Message", description = "Set to false to disable the first Dragon killed message.")
	public static Boolean showFirstKilledDragonMessage = true;

	public DifficultyFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon)
				|| dragon.getDragonFight() == null)
			return;

		CompoundTag dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.DIFFICULTY))
			return;

		int radius = 256;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AABB bb = new AABB(pos1, pos2);

		List<ServerPlayer> players = event.getLevel().getEntitiesOfClass(ServerPlayer.class, bb);

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

		if (!sumKilledDragonDifficulty)
			dragonDifficulty.set(dragonDifficulty.get() / players.size());

		if (players.size() > 1)
			dragonDifficulty.set(dragonDifficulty.get() * (1d + ((players.size() - 1) * bonusDifficultyPerPlayer)));

		dragonTags.putFloat(Strings.Tags.DIFFICULTY, (float) dragonDifficulty.get());
	}

	//Increase Player Difficulty
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon))
			return;

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
				if (difficulty.getKilledDragons() <= startingDifficulty && showFirstKilledDragonMessage)
					player.sendSystemMessage(Component.translatable(Strings.Translatable.FIRST_DRAGON_KILL));
				if (difficulty.getKilledDragons() < maxDifficulty)
					difficulty.addKilledDragons(1);
			});
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void setPlayerData(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;

		player.getCapability(Difficulty.INSTANCE).ifPresent(difficulty -> {
			if (difficulty.getKilledDragons() < startingDifficulty) {
				difficulty.setKilledDragons(startingDifficulty);
				LogHelper.info("[Progressive Bosses] %s killed dragons counter was below the set 'Starting Difficulty', Has been increased to match 'Starting Difficulty'", player.getName().getString());
			}
			if (difficulty.getKilledDragons() > maxDifficulty) {
				difficulty.setKilledDragons(maxDifficulty);
				LogHelper.info("[Progressive Bosses] %s killed dragons counter was above the 'Max Difficulty', Has been decreased to match 'Max Difficulty'", player.getName().getString());
			}

			if (difficulty.getFirstDragon() == 0) {
				difficulty.setFirstDragon((byte) 1);
				LogHelper.info("[Progressive Bosses] %s first spawned. Set First Dragon to 1", player.getName().getString());
			}
		});
	}
}
