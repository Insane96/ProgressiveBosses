package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.RandomHelper;
import insane96mcp.progressivebosses.module.dragon.ai.DragonMinionAttackGoal;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DragonMinionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Minions", description = "Shulkers that will make you float around.")
public class MinionFeature extends Feature {
	private final ForgeConfigSpec.ConfigValue<Integer> minionAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> minCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Double> cooldownReductionConfig;
	private final ForgeConfigSpec.ConfigValue<Double> blindingChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> blindingDurationConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> reducedDragonDamageConfig;

	public int minionAtDifficulty = 1;
	public int minCooldown = 1400;
	public int maxCooldown = 2000;
	public double cooldownReduction = 0.05d;
	public double blindingChance = 0.05d;
	public int blindingDuration = 150;
	public boolean reducedDragonDamage = true;

	public MinionFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		minionAtDifficultyConfig = Config.builder
				.comment("At which difficulty the Ender Dragon starts spawning Minions")
				.defineInRange("Minion at Difficulty", minionAtDifficulty, 0, Integer.MAX_VALUE);
		minCooldownConfig = Config.builder
				.comment("Minimum ticks (20 ticks = 1 seconds) after Minions can spwan.")
				.defineInRange("Minimum Cooldown", minCooldown, 0, Integer.MAX_VALUE);
		maxCooldownConfig = Config.builder
				.comment("Maximum ticks (20 ticks = 1 seconds) after Minions can spwan.")
				.defineInRange("Maximum Cooldown", maxCooldown, 0, Integer.MAX_VALUE);
		cooldownReductionConfig = Config.builder
				.comment("Percentage cooldown reduction per difficulty for the cooldown of Minion spawning.")
				.defineInRange("Cooldown Reduction", cooldownReduction, 0d, 1d);
		blindingChanceConfig = Config.builder
				.comment("Percentage chance per difficulty for a Minion to spawn as a Blinding Minion.")
				.defineInRange("Blinding Chance", blindingChance, 0d, 1d);
		blindingDurationConfig = Config.builder
				.comment("Time (in ticks) for the bliding effect when hit by a blinding bullet.")
				.defineInRange("Blinding duration", blindingDuration, 0, 6000);
		reducedDragonDamageConfig = Config.builder
				.comment("If true, Dragon Minions will take only 10% damage from the Ender Dragon.")
				.define("Reduced Dragon Damage", reducedDragonDamage);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.minionAtDifficulty = this.minionAtDifficultyConfig.get();
		this.minCooldown = this.minCooldownConfig.get();
		this.maxCooldown = this.maxCooldownConfig.get();
		if (this.minCooldown > this.maxCooldown)
			this.minCooldown = this.maxCooldown;
		this.cooldownReduction = this.cooldownReductionConfig.get();
		this.blindingChance = this.blindingChanceConfig.get();
		this.blindingDuration = this.blindingDurationConfig.get();
		this.reducedDragonDamage = this.reducedDragonDamageConfig.get();
	}

	@SubscribeEvent
	public void onDragonSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon dragon))
			return;

		CompoundTag dragonTags = dragon.getPersistentData();

		int cooldown = (int) (RandomHelper.getInt(dragon.getRandom(), this.minCooldown, this.maxCooldown) * 0.5d);
		dragonTags.putInt(Strings.Tags.DRAGON_MINION_COOLDOWN, cooldown);
	}

	@SubscribeEvent
	public void onShulkerSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Shulker shulker))
			return;

		CompoundTag tags = shulker.getPersistentData();
		if (!tags.contains(Strings.Tags.DRAGON_MINION))
			return;

		setMinionAI(shulker);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon dragon))
			return;

		Level world = event.getEntity().level;

		CompoundTag dragonTags = dragon.getPersistentData();

		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);
		if (difficulty < this.minionAtDifficulty)
			return;

		if (dragon.getHealth() <= 0)
			return;

		int cooldown = dragonTags.getInt(Strings.Tags.DRAGON_MINION_COOLDOWN);
		if (cooldown > 0) {
			dragonTags.putInt(Strings.Tags.DRAGON_MINION_COOLDOWN, cooldown - 1);
			return;
		}

		//If there is no player in the main island don't spawn minions
		BlockPos centerPodium = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AABB bb = new AABB(centerPodium).inflate(64d);
		List<ServerPlayer> players = world.getEntitiesOfClass(ServerPlayer.class, bb);

		if (players.isEmpty())
			return;

		int minCooldown = this.minCooldown;
		int maxCooldown = this.maxCooldown;

		cooldown = RandomHelper.getInt(world.random, minCooldown, maxCooldown);
		cooldown *= 1 - this.cooldownReduction * difficulty;
		dragonTags.putInt(Strings.Tags.DRAGON_MINION_COOLDOWN, cooldown - 1);

		float angle = world.random.nextFloat() * (float) Math.PI * 2f;
		float x = (float) (Math.cos(angle) * (RandomHelper.getFloat(dragon.getRandom(), 16f, 45f)));
		float z = (float) (Math.sin(angle) * (RandomHelper.getFloat(dragon.getRandom(), 16f, 45f)));
		float y = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 255, z)).getY();
		Shulker shulker = summonMinion(world, new Vec3(x, y, z), difficulty);
	}

	private static void setMinionAI(Shulker shulker) {
		ArrayList<Goal> toRemove = new ArrayList<>();
		shulker.goalSelector.availableGoals.forEach(goal -> {
			if (goal.getGoal() instanceof Shulker.ShulkerAttackGoal)
				toRemove.add(goal.getGoal());
		});
		toRemove.forEach(shulker.goalSelector::removeGoal);
		shulker.goalSelector.addGoal(2, new DragonMinionAttackGoal(shulker, 70));

		toRemove.clear();
		shulker.targetSelector.availableGoals.forEach(goal -> {
			if (goal.getGoal() instanceof NearestAttackableTargetGoal)
				toRemove.add(goal.getGoal());
			if (goal.getGoal() instanceof HurtByTargetGoal)
				toRemove.add(goal.getGoal());
		});
		toRemove.forEach(shulker.targetSelector::removeGoal);

		shulker.targetSelector.addGoal(2, new ILNearestAttackableTargetGoal<>(shulker, Player.class, false).setIgnoreLineOfSight());
		shulker.targetSelector.addGoal(1, new HurtByTargetGoal(shulker, Shulker.class, EnderDragon.class));
	}

	public Shulker summonMinion(Level world, Vec3 pos, float difficulty) {
		Shulker shulker = EntityType.SHULKER.create(world);
		CompoundTag minionTags = shulker.getPersistentData();
		minionTags.putBoolean(Strings.Tags.DRAGON_MINION, true);

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		boolean isBlindingMinion = world.getRandom().nextDouble() < this.blindingChance * difficulty;

		shulker.setPos(pos.x, pos.y, pos.z);
		shulker.setCustomName(new TranslatableComponent(Strings.Translatable.DRAGON_MINION));
		shulker.lootTable = BuiltInLootTables.EMPTY;
		shulker.setPersistenceRequired();
		DragonMinionHelper.setMinionColor(shulker, isBlindingMinion);

		MCUtils.applyModifier(shulker, Attributes.FOLLOW_RANGE, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS_UUID, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS, 64, AttributeModifier.Operation.ADDITION);

		world.addFreshEntity(shulker);
		return shulker;
	}

	@SubscribeEvent
	public void onMinionHurt(LivingHurtEvent event) {
		if (!this.isEnabled())
			return;

		if (!this.reducedDragonDamage)
			return;

		if (!(event.getEntity() instanceof Shulker shulker))
			return;

		CompoundTag compoundNBT = shulker.getPersistentData();
		if (!compoundNBT.contains(Strings.Tags.DRAGON_MINION))
			return;

		if (event.getSource().getEntity() instanceof EnderDragon)
			event.setAmount(event.getAmount() * 0.1f);
	}

	public void onBulletTick(ShulkerBullet shulkerBulletEntity) {
		if (!shulkerBulletEntity.level.isClientSide && shulkerBulletEntity.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET)) {
			((ServerLevel)shulkerBulletEntity.level).sendParticles(ParticleTypes.ENTITY_EFFECT, shulkerBulletEntity.getX(), shulkerBulletEntity.getY(), shulkerBulletEntity.getZ(), 1, 0d, 0d, 0d, 0d);
		}
	}
}
