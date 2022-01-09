package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.capability.Difficulty;
import insane96mcp.progressivebosses.capability.IDifficulty;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Label(name = "Difficulty Settings", description = "How difficulty is handled for the Wither.")
public class DifficultyFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> spawnRadiusPlayerCheckConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> sumSpawnedWitherDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusDifficultyPerPlayerConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> startingDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> showFirstSummonedWitherMessageConfig;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> entityBlacklistConfig;

	private static final List<String> defaultEntityBlacklist = Arrays.asList("botania:pink_wither");

	public int spawnRadiusPlayerCheck = 128;
	public boolean sumSpawnedWitherDifficulty = false;
	public double bonusDifficultyPerPlayer = 0.25d;
	public int maxDifficulty = 24;
	public int startingDifficulty = 0;
	public boolean showFirstSummonedWitherMessage = true;
	public List<String> entityBlacklist = defaultEntityBlacklist;

	public DifficultyFeature(Module module) {
		super(Config.builder, module, true, false);
		this.pushConfig(Config.builder);
		spawnRadiusPlayerCheckConfig = Config.builder
				.comment("How much blocks from wither will be scanned for players to check for difficulty")
				.defineInRange("Spawn Radius Player Check", spawnRadiusPlayerCheck, 16, Integer.MAX_VALUE);
		sumSpawnedWitherDifficultyConfig = Config.builder
				.comment("If false and there's more than 1 player around the Wither, difficulty will be the average of all the players' difficulty instead of summing them.")
				.define("Sum Spawned Wither Difficulty", sumSpawnedWitherDifficulty);
		bonusDifficultyPerPlayerConfig = Config.builder
				.comment("Percentage bonus difficulty added to the Wither when more than one player is present. Each player past the first one will add this percentage to the difficulty.")
				.defineInRange("Bonus Difficulty per Player", this.bonusDifficultyPerPlayer, 0d, 24d);
		maxDifficultyConfig = Config.builder
				.comment("The Maximum difficulty (times spawned) reachable by Wither.")
				.defineInRange("Max Difficulty", maxDifficulty, 1, Integer.MAX_VALUE);
		startingDifficultyConfig = Config.builder
				.comment("How much difficulty will players start with when joining a world? Note that this will apply when the first Wither is spawned so if the player has already spawned one this will not apply.")
				.defineInRange("Starting Difficulty", startingDifficulty, 0, Integer.MAX_VALUE);
		this.showFirstSummonedWitherMessageConfig = Config.builder
				.comment("Set to false to disable the first Wither summoned message.")
				.define("Show First Summoned Wither Message", this.showFirstSummonedWitherMessage);
		entityBlacklistConfig = Config.builder
				.comment("Entities that extend the vanilla Wither but shouldn't be taken into account by the mod (e.g. Botania's Pink Wither).")
				.defineList("Entity Blacklist", entityBlacklist, o -> o instanceof String);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.spawnRadiusPlayerCheck = this.spawnRadiusPlayerCheckConfig.get();
		this.sumSpawnedWitherDifficulty = this.sumSpawnedWitherDifficultyConfig.get();
		this.bonusDifficultyPerPlayer = this.bonusDifficultyPerPlayerConfig.get();
		this.maxDifficulty = this.maxDifficultyConfig.get();
		this.startingDifficulty = this.startingDifficultyConfig.get();
		this.showFirstSummonedWitherMessage = this.showFirstSummonedWitherMessageConfig.get();

		//entityBlacklist
		this.entityBlacklist = new ArrayList<>();
		for (String string : this.entityBlacklistConfig.get()) {
			if (!ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(string)))
				LogHelper.warn("Entity %s for Wither's Difficulty Feature entityBlacklist doesn't exist, will be ignored.", string);
			this.entityBlacklist.add(string);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherBoss))
			return;

		if (this.entityBlacklist.contains(event.getEntity().getType().getRegistryName().toString()))
			return;

		WitherBoss wither = (WitherBoss) event.getEntity();

		CompoundTag witherTags = wither.getPersistentData();
		if (witherTags.contains(Strings.Tags.DIFFICULTY))
			return;

		BlockPos pos1 = wither.blockPosition().offset(-this.spawnRadiusPlayerCheck, -this.spawnRadiusPlayerCheck, -this.spawnRadiusPlayerCheck);
		BlockPos pos2 = wither.blockPosition().offset(this.spawnRadiusPlayerCheck, this.spawnRadiusPlayerCheck, this.spawnRadiusPlayerCheck);
		AABB bb = new AABB(pos1, pos2);

		List<ServerPlayer> players = event.getWorld().getEntitiesOfClass(ServerPlayer.class, bb);
		if (players.size() == 0)
			return;

		float witherDifficulty = 0;

		for (ServerPlayer player : players) {
			IDifficulty difficulty = player.getCapability(Difficulty.INSTANCE).orElse(null);
			witherDifficulty += difficulty.getSpawnedWithers();
			if (difficulty.getSpawnedWithers() >= this.maxDifficulty)
				continue;
			if (difficulty.getKilledDragons() <= this.startingDifficulty && this.showFirstSummonedWitherMessage)
				player.sendMessage(new TranslatableComponent(Strings.Translatable.FIRST_WITHER_SUMMON), Util.NIL_UUID);
			difficulty.addSpawnedWithers(1);
		}

		if (!this.sumSpawnedWitherDifficulty)
			witherDifficulty /= players.size();

		if (players.size() > 1)
			witherDifficulty *= 1d + ((players.size() - 1) * this.bonusDifficultyPerPlayer);

		witherTags.putFloat(Strings.Tags.DIFFICULTY, witherDifficulty);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void setPlayerData(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ServerPlayer))
			return;

		ServerPlayer player = (ServerPlayer) event.getEntity();

		IDifficulty difficulty = player.getCapability(Difficulty.INSTANCE).orElse(null);

		if (difficulty.getSpawnedWithers() < this.startingDifficulty) {
			difficulty.setSpawnedWithers(this.startingDifficulty);
			LogHelper.info("[Progressive Bosses] %s spawned withers counter was below the set 'Starting Difficulty', Has been increased to match 'Starting Difficulty'", player.getName().getString());
		}
	}
}
