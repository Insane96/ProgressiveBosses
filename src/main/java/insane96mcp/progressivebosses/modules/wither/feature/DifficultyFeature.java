package insane96mcp.progressivebosses.modules.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@Label(name = "Difficulty Settings", description = "How difficulty is handled for the Wither. Disabling this \"Feature\" will disable all the Dragon changes.")
public class DifficultyFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> spawnRadiusPlayerCheckConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> sumSpawnedWitherDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> startingDifficultyConfig;

	public int spawnRadiusPlayerCheck = 128;
	public boolean sumSpawnedWitherDifficulty = false;
	public int maxDifficulty = 72;
	public int startingDifficulty = 0;

	public DifficultyFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		spawnRadiusPlayerCheckConfig = Config.builder
				.comment("How much blocks from wither will be scanned for players to check for difficulty")
				.defineInRange("Spawn Radius Player Check", spawnRadiusPlayerCheck, 16, Integer.MAX_VALUE);
		sumSpawnedWitherDifficultyConfig = Config.builder
				.comment("If true and there are more players around the Wither, the Wither will have his stats based on the sum of both players difficulty. If false, the Wither stats will be based on the average of the difficulty of the players around")
				.define("Sum Spawned Wither Difficulty", sumSpawnedWitherDifficulty);
		maxDifficultyConfig = Config.builder
				.comment("The Maximum difficulty (times spawned) reachable by Wither. By default is set to 72 because the Wither reaches the maximum amount of health (1024, capped by Minecraft. Some mods can increase this) after 72 withers spawned.")
				.defineInRange("Max Difficulty", maxDifficulty, 1, Integer.MAX_VALUE);
		startingDifficultyConfig = Config.builder
				.comment("How much difficulty will players start with when joining a world? Note that this will apply when the first Wither is spawned so if the player has already spawned one this will not apply.")
				.defineInRange("Starting Difficulty", startingDifficulty, 0, Integer.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		spawnRadiusPlayerCheck = spawnRadiusPlayerCheckConfig.get();
		sumSpawnedWitherDifficulty = sumSpawnedWitherDifficultyConfig.get();
		maxDifficulty = maxDifficultyConfig.get();
		startingDifficulty = startingDifficultyConfig.get();
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();

		CompoundNBT witherTags = wither.getPersistentData();
		if (witherTags.contains(Strings.Tags.DIFFICULTY))
			return;

		BlockPos pos1 = wither.getPosition().add(-this.spawnRadiusPlayerCheck, -this.spawnRadiusPlayerCheck, -this.spawnRadiusPlayerCheck);
		BlockPos pos2 = wither.getPosition().add(this.spawnRadiusPlayerCheck, this.spawnRadiusPlayerCheck, this.spawnRadiusPlayerCheck);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);

		List<ServerPlayerEntity> players = event.getWorld().getLoadedEntitiesWithinAABB(ServerPlayerEntity.class, bb);
		float spawnedTotal = 0;
		//If no players are found in the "Spawn Radius Player Check", try to get the nearest player
		if (players.size() == 0) {
			PlayerEntity nearestPlayer = event.getWorld().getClosestPlayer(wither.getPosX(), wither.getPosY(), wither.getPosZ(), Double.MAX_VALUE, true);
			if (nearestPlayer instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) nearestPlayer;
				CompoundNBT playerTags = player.getPersistentData();
				int spawnedWithers = playerTags.getInt(Strings.Tags.SPAWNED_WITHERS);
				spawnedTotal += spawnedWithers;
				if (spawnedWithers < this.maxDifficulty)
					playerTags.putInt(Strings.Tags.SPAWNED_WITHERS, spawnedWithers + 1);
			}
		}
		//Otherwise sum the players' difficulties
		else {
			for (ServerPlayerEntity player : players) {
				CompoundNBT playerTags = player.getPersistentData();
				int spawnedWithers = playerTags.getInt(Strings.Tags.SPAWNED_WITHERS);
				spawnedTotal += spawnedWithers;
				if (spawnedWithers >= this.maxDifficulty)
					continue;
				playerTags.putInt(Strings.Tags.SPAWNED_WITHERS, spawnedWithers + 1);
			}
		}

		//If still no players are found then surrender
		//if (spawnedTotal == 0)
		//return;

		if (!this.sumSpawnedWitherDifficulty)
			spawnedTotal /= players.size();

		witherTags.putFloat(Strings.Tags.DIFFICULTY, spawnedTotal);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void setPlayerData(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ServerPlayerEntity))
			return;

		ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();

		CompoundNBT playerTags = player.getPersistentData();
		if (!playerTags.contains(Strings.Tags.SPAWNED_WITHERS))
			playerTags.putInt(Strings.Tags.SPAWNED_WITHERS, this.startingDifficulty);
	}
}
