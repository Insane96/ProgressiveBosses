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
import insane96mcp.progressivebosses.module.wither.block.CorruptedSoulSandBlockEntity;
import insane96mcp.progressivebosses.module.wither.data.*;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBBlocks;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
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

	public static final List<WitherStats> DEFAULT_WITHER_STATS = new ArrayList<>(List.of(
			new WitherStats(0,
					new WitherAttackStats(8f, 2f, 45, 70, 0.05f, 12f, 50, 0.05f, 40),
					new WitherHealthStats(300f, 1f, 0.8f, 30),
					new WitherResistancesWeaknesses(0f, 0.2f, 200f),
					new WitherMiscStats(7f, false, false, false)),
			new WitherStats(1,
					new WitherAttackStats(12f, 2.2f, 40, 65, 0.05f, 16f, 45, 0.05f, 60),
					new WitherHealthStats(400f, 1.1f, 0.7f, 30),
					new WitherResistancesWeaknesses(0.05f, 0.25f, 225f),
					new WitherMiscStats(8.5f, false, true, false)),
			new WitherStats(2,
					new WitherAttackStats(16f, 2.4f, 35, 60, 0.05f, 20f, 40, 0.05f, 80),
					new WitherHealthStats(500f, 1.25f, 0.6f, 35),
					new WitherResistancesWeaknesses(0.1f, 0.30f, 250f),
					new WitherMiscStats(10f, false, true, false)),
			new WitherStats(3,
					new WitherAttackStats(20f, 2.6f, 30, 55, 0.05f, 24f, 35, 0.05f, 100),
					new WitherHealthStats(600f, 1.5f, 0.6f, 40),
					new WitherResistancesWeaknesses(0.15f, 0.35f, 275f),
					new WitherMiscStats(11.5f, true, true, true))
	));

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
		if (players.isEmpty())
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

	@Nullable
	private static BlockPattern witherPatternFull;
	@Nullable
	private static BlockPattern witherPatternBase;

	@SubscribeEvent
	public void onSkullPlaced(BlockEvent.EntityPlaceEvent event) {
		BlockState state = event.getState();
		BlockPos pos = event.getPos();
		Level level = (Level) event.getLevel();
		boolean isWitherSkeletonSkull = state.is(Blocks.WITHER_SKELETON_SKULL) || state.is(Blocks.WITHER_SKELETON_WALL_SKULL);
        if (!isWitherSkeletonSkull || pos.getY() < level.getMinBuildHeight() || level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL)
            return;

        BlockPattern.BlockPatternMatch blockPatternMatch = getOrCreatePBWitherFull().find(level, pos);
        if (blockPatternMatch == null)
            return;
			
        PBWither wither = PBEntities.WITHER.get().create(level);
        if (wither != null) {
			int lvl = getLevelFromPatternBlocks(level, blockPatternMatch);
            CarvedPumpkinBlock.clearPatternBlocks(level, blockPatternMatch);
            BlockPos blockpos = blockPatternMatch.getBlock(1, 2, 0).getPos();
            wither.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.55D, (double)blockpos.getZ() + 0.5D, blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
            wither.yBodyRot = blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
            wither.makeInvulnerable();
			wither.setLvl(lvl);

            for (ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, wither.getBoundingBox().inflate(50.0D))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, wither);
            }

            level.addFreshEntity(wither);
            CarvedPumpkinBlock.updatePatternBlocks(level, blockPatternMatch);
        }

    }

	private static BlockPattern getOrCreatePBWitherFull() {
		if (witherPatternFull == null) {
			witherPatternFull = BlockPatternBuilder
					.start()
					.aisle("^^^", "#C#", "~#~")
					.where('#', (blockInWorld) -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
					.where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL))))
					.where('~', (blockInWorld) -> blockInWorld.getState().canBeReplaced())
					.where('C', (blockInWorld) -> blockInWorld.getState().is(PBBlocks.CORRUPTED_SOUL_SAND.get()))
					.build();
		}

		return witherPatternFull;
	}

	//TODO Dispenser
	private static BlockPattern getOrCreatePBWitherBase() {
		if (witherPatternBase == null) {
			witherPatternBase = BlockPatternBuilder
					.start()
					.aisle("   ", "###", "~#~")
					.where('#', (blockInWorld) -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
					.where('~', (blockInWorld) -> blockInWorld.getState().isAir()).build();
		}

		return witherPatternBase;
	}

	public static int getLevelFromPatternBlocks(Level pLevel, BlockPattern.BlockPatternMatch pPatternMatch) {
		for (int i = 0; i < pPatternMatch.getWidth(); ++i) {
			for (int j = 0; j < pPatternMatch.getHeight(); ++j) {
				BlockInWorld blockinworld = pPatternMatch.getBlock(i, j, 0);
				if (blockinworld.getState().is(PBBlocks.CORRUPTED_SOUL_SAND.get())
						&& (pLevel.getBlockEntity(blockinworld.getPos()) instanceof CorruptedSoulSandBlockEntity corruptedSoulSandBlockEntity)) {
					return corruptedSoulSandBlockEntity.getLvl();
				}
			}
		}
		return -1;
	}
}
