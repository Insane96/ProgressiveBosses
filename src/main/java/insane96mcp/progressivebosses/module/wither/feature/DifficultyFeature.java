package insane96mcp.progressivebosses.module.wither.feature;

import com.google.common.util.concurrent.AtomicDouble;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.IdTagMatcher;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.capability.Difficulty;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@Label(name = "Difficulty Settings", description = "How difficulty is handled for the Wither.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class DifficultyFeature extends Feature {
	@Config(min = 1)
	@Label(name = "Max Difficulty", description = "The Maximum difficulty (times spawned) reachable by Wither.")
	public static Integer maxDifficulty = 8;
	@Config(min = 0)
	@Label(name = "Starting Difficulty", description = "How much difficulty will players start with when joining a world? Note that this will apply when the first Wither is spawned so if the player has already spawned one this will not apply.")
	public static Integer startingDifficulty = 0;
	@Config(min = 16)
	@Label(name = "Spawn Radius Player Check", description = "How much blocks from wither will be scanned for players to check for difficulty")
	public static Integer spawnRadiusPlayerCheck = 128;
	@Config
	@Label(name = "Sum Spawned Wither Difficulty", description = "If false and there's more than 1 player around the Wither, difficulty will be the average of all the players' difficulty instead of summing them.")
	public static Boolean sumSpawnedWitherDifficulty = false;
	@Config(min = 0d)
	@Label(name = "Bonus Difficulty per Player", description = "Percentage bonus difficulty added to the Wither when more than one player is present. Each player past the first one will add this percentage to the difficulty.")
	public static Double bonusDifficultyPerPlayer = 0.25d;
	@Config(min = 16)
	@Label(name = "Show First Summoned Wither Message", description = "Set to false to disable the first Wither summoned message.")
	public static Boolean showFirstSummonedWitherMessage = true;
	@Config
	@Label(name = "Entity Blacklist", description = "Entities that extend the vanilla Wither but shouldn't be taken into account by the mod (e.g. Botania's Pink Wither).")
	public static Blacklist entityBlacklist = new Blacklist(List.of(
			new IdTagMatcher(IdTagMatcher.Type.ID, "botania:pink_wither")
	), false);

	public DifficultyFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);

	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof WitherBoss wither)
				|| entityBlacklist.isEntityBlackOrNotWhitelist(wither))
			return;

		CompoundTag witherTags = wither.getPersistentData();
		if (witherTags.contains(Strings.Tags.DIFFICULTY))
			return;

		BlockPos pos1 = wither.blockPosition().offset(-spawnRadiusPlayerCheck, -spawnRadiusPlayerCheck, -spawnRadiusPlayerCheck);
		BlockPos pos2 = wither.blockPosition().offset(spawnRadiusPlayerCheck, spawnRadiusPlayerCheck, spawnRadiusPlayerCheck);
		AABB bb = new AABB(pos1, pos2);

		List<ServerPlayer> players = event.getLevel().getEntitiesOfClass(ServerPlayer.class, bb);
		if (players.size() == 0)
			return;

		final AtomicDouble witherDifficulty = new AtomicDouble(0d);

		for (ServerPlayer player : players) {
			player.getCapability(Difficulty.INSTANCE).ifPresent(difficulty -> {
				witherDifficulty.addAndGet(difficulty.getSpawnedWithers());
				if (difficulty.getSpawnedWithers() >= maxDifficulty)
					return;
				if (difficulty.getSpawnedWithers() <= startingDifficulty && showFirstSummonedWitherMessage)
					player.sendSystemMessage(Component.translatable(Strings.Translatable.FIRST_WITHER_SUMMON));
				difficulty.addSpawnedWithers(1);
			});
		}

		if (!sumSpawnedWitherDifficulty)
			witherDifficulty.set(witherDifficulty.get() / players.size());

		if (players.size() > 1)
			witherDifficulty.set(witherDifficulty.get() * (1d + ((players.size() - 1) * bonusDifficultyPerPlayer)));

		witherTags.putFloat(Strings.Tags.DIFFICULTY, (float) witherDifficulty.get());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void setPlayerData(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		player.getCapability(Difficulty.INSTANCE).ifPresent(difficulty -> {
			if (difficulty.getSpawnedWithers() < startingDifficulty) {
				difficulty.setSpawnedWithers(startingDifficulty);
				LogHelper.info("[Progressive Bosses] %s spawned withers counter was below the set 'Starting Difficulty', Has been increased to match 'Starting Difficulty'", player.getName().getString());
			}
		});
	}
}
