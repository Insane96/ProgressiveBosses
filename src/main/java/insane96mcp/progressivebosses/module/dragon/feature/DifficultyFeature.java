package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.capability.DifficultyCapability;
import insane96mcp.progressivebosses.capability.IDifficulty;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@Label(name = "Difficulty Settings", description = "How difficulty is handled for the Dragon.")
public class DifficultyFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> sumKilledDragonDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> startingDifficultyConfig;

	public boolean sumKilledDragonDifficulty = false;
	public int maxDifficulty = 24;
	public int startingDifficulty = 0;

	public DifficultyFeature(Module module) {
		super(Config.builder, module, true, false);
		Config.builder.comment(this.getDescription()).push(this.getName());
		sumKilledDragonDifficultyConfig = Config.builder
				.comment("If true and there are more players around the Dragon, she will have his stats based on the sum of both players' difficulty. If false, the Dragon stats will be based on the average of the difficulty of the players around.")
				.define("Sum Killed Dragons Difficulty", sumKilledDragonDifficulty);
		maxDifficultyConfig = Config.builder
				.comment("The Maximum difficulty (times killed) reachable by Ender Dragon. By default is set to 24 because it's the last spawning end gate.")
				.defineInRange("Max Difficulty", maxDifficulty, 1, Integer.MAX_VALUE);
		startingDifficultyConfig = Config.builder
				.comment("How much difficulty will players start with when joining a world? Note that this will apply when the player joins the world if the current player difficulty is below this value.")
				.defineInRange("Starting Difficulty", startingDifficulty, 0, Integer.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		sumKilledDragonDifficulty = sumKilledDragonDifficultyConfig.get();
		maxDifficulty = maxDifficultyConfig.get();
		startingDifficulty = startingDifficultyConfig.get();
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!event.getWorld().getDimensionKey().getLocation().equals(DimensionType.THE_END.getLocation()))
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

		if (dragon.getFightManager() == null)
			return;

		CompoundNBT dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.DIFFICULTY))
			return;

		int radius = 256;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);

		List<ServerPlayerEntity> players = event.getWorld().getLoadedEntitiesWithinAABB(ServerPlayerEntity.class, bb);

		int playersFirstDragon = 0;
		float killedTotal = 0;
		//If no players are found in the "Spawn Radius Player Check", try to get the nearest player
		if (players.size() == 0) {
			ServerPlayerEntity nearestPlayer = (ServerPlayerEntity) event.getWorld().getClosestPlayer(dragon.getPosX(), dragon.getPosY(), dragon.getPosZ(), Double.MAX_VALUE, true);
			players.add(nearestPlayer);
		}

		for (ServerPlayerEntity player : players) {
			IDifficulty difficulty = player.getCapability(DifficultyCapability.DIFFICULTY).orElse(null);
			killedTotal += difficulty.getKilledDragons();
			if (difficulty.getFirstDragon() == (byte) 1) {
				playersFirstDragon++;
				difficulty.setFirstDragon((byte) 2);
			}
		}

		dragonTags.putInt(Strings.Tags.EGGS_TO_DROP, playersFirstDragon);

		if (!this.sumKilledDragonDifficulty)
			killedTotal /= players.size();

		dragonTags.putFloat(Strings.Tags.DIFFICULTY, killedTotal);
	}

	//Increase Player Difficulty
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntity().world.isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

		int radius = 256;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);

		List<ServerPlayerEntity> players = dragon.world.getLoadedEntitiesWithinAABB(ServerPlayerEntity.class, bb);
		//If no players are found in the "Spawn Radius Player Check", try to get the nearest player
		if (players.size() == 0) {
			ServerPlayerEntity nearestPlayer = (ServerPlayerEntity) dragon.world.getClosestPlayer(dragon.getPosX(), dragon.getPosY(), dragon.getPosZ(), Double.MAX_VALUE, true);
			players.add(nearestPlayer);
		}

		for (ServerPlayerEntity player : players) {
			IDifficulty difficulty = player.getCapability(DifficultyCapability.DIFFICULTY).orElse(null);
			if (difficulty.getKilledDragons() < this.maxDifficulty)
				difficulty.addKilledDragons(1);
		}
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

		IDifficulty difficulty = player.getCapability(DifficultyCapability.DIFFICULTY).orElse(null);

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
	}
}
