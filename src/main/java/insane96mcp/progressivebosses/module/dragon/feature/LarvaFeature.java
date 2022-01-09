package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.MCUtils;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.dragon.ai.PBNearestAttackableTargetGoal;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Larva", description = "Mini things that are just annoying.")
public class LarvaFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> larvaAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> bonusLarvaEveryDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxSpawnedConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> minCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> dragonImmuneConfig;

	public int larvaAtDifficulty = 1;
	public int bonusLarvaEveryDifficulty = 2;
	public int maxSpawned = 7;
	public int minCooldown = 800;
	public int maxCooldown = 1400;
	public boolean dragonImmune = true;

	public LarvaFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		larvaAtDifficultyConfig = Config.builder
				.comment("At which difficulty the Ender Dragon starts spawning Larvae")
				.defineInRange("Larva at Difficulty", larvaAtDifficulty, 0, Integer.MAX_VALUE);
		bonusLarvaEveryDifficultyConfig = Config.builder
				.comment("As the Wither starts spawning Minions, every how much difficulty the Wither will spawn one more Minion")
				.defineInRange("Bonus Larva Every Difficulty", bonusLarvaEveryDifficulty, 0, Integer.MAX_VALUE);
		maxSpawnedConfig = Config.builder
				.comment("Maximum Larva spawned by the Ender Dragon")
				.defineInRange("Max Larvae Spawned", maxSpawned, 0, Integer.MAX_VALUE);
		minCooldownConfig = Config.builder
				.comment("Minimum ticks (20 ticks = 1 seconds) after Minions can spwan.")
				.defineInRange("Minimum Cooldown", minCooldown, 0, Integer.MAX_VALUE);
		maxCooldownConfig = Config.builder
				.comment("Maximum ticks (20 ticks = 1 seconds) after Minions can spwan.")
				.defineInRange("Maximum Cooldown", maxCooldown, 0, Integer.MAX_VALUE);
		dragonImmuneConfig = Config.builder
				.comment("Dragon Minions are immune to any damage from the Ender Dragon, either direct or Acid.")
				.define("Dragon Immune", dragonImmune);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.larvaAtDifficulty = this.larvaAtDifficultyConfig.get();
		this.bonusLarvaEveryDifficulty = this.bonusLarvaEveryDifficultyConfig.get();
		this.maxSpawned = this.maxSpawnedConfig.get();
		this.minCooldown = this.minCooldownConfig.get();
		this.maxCooldown = this.maxCooldownConfig.get();
		if (this.minCooldown > this.maxCooldown)
			this.minCooldown = this.maxCooldown;
		this.dragonImmune = this.dragonImmuneConfig.get();
	}

	@SubscribeEvent
	public void onDragonSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

		CompoundNBT dragonTags = dragon.getPersistentData();

		int cooldown = (int) (RandomHelper.getInt(dragon.getRandom(), this.minCooldown, this.maxCooldown) * 0.5d);
		dragonTags.putInt(Strings.Tags.DRAGON_LARVA_COOLDOWN, cooldown);
	}

	@SubscribeEvent
	public void onLarvaSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EndermiteEntity))
			return;

		EndermiteEntity endermite = (EndermiteEntity) event.getEntity();

		CompoundNBT tags = endermite.getPersistentData();
		if (!tags.contains(Strings.Tags.DRAGON_LARVA))
			return;

		setLarvaAI(endermite);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		World world = event.getEntity().level;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
		CompoundNBT dragonTags = dragon.getPersistentData();

		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);
		if (difficulty < this.larvaAtDifficulty)
			return;

		if (dragon.getHealth() <= 0)
			return;

		int cooldown = dragonTags.getInt(Strings.Tags.DRAGON_LARVA_COOLDOWN);
		if (cooldown > 0) {
			dragonTags.putInt(Strings.Tags.DRAGON_LARVA_COOLDOWN, cooldown - 1);
			return;
		}

		//If there is no player in the main island don't spawn larvae
		BlockPos centerPodium = dragon.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AxisAlignedBB bb = new AxisAlignedBB(centerPodium).inflate(96d);
		List<ServerPlayerEntity> players = world.getLoadedEntitiesOfClass(ServerPlayerEntity.class, bb);

		if (players.isEmpty())
			return;

		int minCooldown = this.minCooldown;
		int maxCooldown = this.maxCooldown;

		cooldown = RandomHelper.getInt(world.random, minCooldown, maxCooldown);
		dragonTags.putInt(Strings.Tags.DRAGON_LARVA_COOLDOWN, cooldown - 1);

		int larvaSpawnedCount = 0;
		for (int i = this.larvaAtDifficulty; i <= difficulty; i += this.bonusLarvaEveryDifficulty) {
			float angle = world.random.nextFloat() * (float) Math.PI * 2f;
			float x = (float) Math.floor(Math.cos(angle) * 3.33f);
			float z = (float) Math.floor(Math.sin(angle) * 3.33f);
			int y = world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 255, z)).getY();
			EndermiteEntity endermite = summonLarva(world, new Vector3d(x + 0.5, y, z + 0.5), difficulty);
			larvaSpawnedCount++;
			if (larvaSpawnedCount >= this.maxSpawned)
				break;
		}
	}

	private static void setLarvaAI(EndermiteEntity endermite) {
		ArrayList<Goal> toRemove = new ArrayList<>();
		endermite.targetSelector.availableGoals.forEach(goal -> {
			if (goal.getGoal() instanceof NearestAttackableTargetGoal)
				toRemove.add(goal.getGoal());
		});
		toRemove.forEach(endermite.targetSelector::removeGoal);

		endermite.targetSelector.addGoal(2, new PBNearestAttackableTargetGoal(endermite));
	}

	public EndermiteEntity summonLarva(World world, Vector3d pos, float difficulty) {
		EndermiteEntity endermite = new EndermiteEntity(EntityType.ENDERMITE, world);
		CompoundNBT minionTags = endermite.getPersistentData();
		minionTags.putBoolean(Strings.Tags.DRAGON_LARVA, true);

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		endermite.setPos(pos.x, pos.y, pos.z);
		endermite.setCustomName(new TranslationTextComponent(Strings.Translatable.DRAGON_LARVA));
		endermite.lootTable = LootTables.EMPTY;
		endermite.setPersistenceRequired();

		MCUtils.applyModifier(endermite, Attributes.FOLLOW_RANGE, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS_UUID, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS, 64, AttributeModifier.Operation.ADDITION);
		MCUtils.applyModifier(endermite, Attributes.MOVEMENT_SPEED, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS_UUID, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS, 0.65d, AttributeModifier.Operation.MULTIPLY_BASE);
		MCUtils.applyModifier(endermite, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, -0.5, AttributeModifier.Operation.MULTIPLY_BASE);
		MCUtils.applyModifier(endermite, Attributes.ATTACK_DAMAGE, Strings.AttributeModifiers.ATTACK_DAMAGE_BONUS_UUID, Strings.AttributeModifiers.ATTACK_DAMAGE_BONUS, 0.35 * difficulty, AttributeModifier.Operation.ADDITION);
		MCUtils.applyModifier(endermite, ForgeMod.SWIM_SPEED.get(), Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 3d, AttributeModifier.Operation.MULTIPLY_BASE);

		world.addFreshEntity(endermite);
		return endermite;
	}

	@SubscribeEvent
	public void onLarvaHurt(LivingAttackEvent event) {
		if (!this.isEnabled())
			return;

		if (!this.dragonImmune)
			return;

		if (!(event.getEntity() instanceof EndermiteEntity))
			return;

		EndermiteEntity endermite = (EndermiteEntity) event.getEntity();
		CompoundNBT compoundNBT = endermite.getPersistentData();
		if (!compoundNBT.contains(Strings.Tags.DRAGON_LARVA))
			return;

		if (event.getSource().getEntity() instanceof EnderDragonEntity || event.getSource().getDirectEntity() instanceof EnderDragonEntity)
			event.setCanceled(true);
	}
}
