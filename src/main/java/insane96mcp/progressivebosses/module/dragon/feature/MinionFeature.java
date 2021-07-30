package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.dragon.ai.DragonMinionAttackGoal;
import insane96mcp.progressivebosses.module.dragon.ai.PBNearestAttackableTargetGoal;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.utils.DragonMinionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
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
	private final ForgeConfigSpec.ConfigValue<Boolean> dragonImmuneConfig;

	public int minionAtDifficulty = 1;
	public int minCooldown = 1400;
	public int maxCooldown = 2000;
	public double cooldownReduction = 0.017d;
	public double blindingChance = 0.017d;
	public boolean dragonImmune = true;

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
		dragonImmuneConfig = Config.builder
				.comment("Dragon Minions are immune to any damage from the Ender Dragon, either direct or Acid.")
				.define("Dragon Immune", dragonImmune);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.minionAtDifficulty = this.minionAtDifficultyConfig.get();
		this.minCooldown = this.minCooldownConfig.get();
		this.maxCooldown = this.maxCooldownConfig.get();
		this.cooldownReduction = this.cooldownReductionConfig.get();
		this.blindingChance = this.blindingChanceConfig.get();
		this.dragonImmune = this.dragonImmuneConfig.get();
	}

	@SubscribeEvent
	public void onDragonSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

		CompoundNBT dragonTags = dragon.getPersistentData();

		int cooldown = (int) (RandomHelper.getInt(dragon.getRNG(), this.minCooldown, this.maxCooldown) * 0.5d);
		dragonTags.putInt(Strings.Tags.DRAGON_MINION_COOLDOWN, cooldown);
	}

	@SubscribeEvent
	public void onShulkerSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ShulkerEntity))
			return;

		ShulkerEntity shulker = (ShulkerEntity) event.getEntity();

		CompoundNBT tags = shulker.getPersistentData();
		if (!tags.contains(Strings.Tags.DRAGON_MINION))
			return;

		setMinionAI(shulker);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().world.isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		World world = event.getEntity().world;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
		CompoundNBT dragonTags = dragon.getPersistentData();

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
		BlockPos centerPodium = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AxisAlignedBB bb = new AxisAlignedBB(centerPodium).grow(96d);
		List<ServerPlayerEntity> players = world.getLoadedEntitiesWithinAABB(ServerPlayerEntity.class, bb);

		if (players.isEmpty())
			return;

		int minCooldown = this.minCooldown;
		int maxCooldown = this.maxCooldown;

		cooldown = RandomHelper.getInt(world.rand, minCooldown, maxCooldown);
		cooldown *= 1 - this.cooldownReduction * difficulty;
		dragonTags.putInt(Strings.Tags.DRAGON_MINION_COOLDOWN, cooldown - 1);

		float angle = world.rand.nextFloat() * (float) Math.PI * 2f;
		float x = (float) (Math.cos(angle) * (RandomHelper.getFloat(dragon.getRNG(), 16f, 46f)));
		float z = (float) (Math.sin(angle) * (RandomHelper.getFloat(dragon.getRNG(), 16f, 46f)));
		float y = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 255, z)).getY();
		ShulkerEntity shulker = summonMinion(world, new Vector3d(x, y, z), difficulty);
	}

	private static void setMinionAI(ShulkerEntity shulker) {
		ArrayList<Goal> toRemove = new ArrayList<>();
		shulker.goalSelector.goals.forEach(goal -> {
			if (goal.getGoal() instanceof ShulkerEntity.AttackGoal)
				toRemove.add(goal.getGoal());
		});
		toRemove.forEach(shulker.goalSelector::removeGoal);
		shulker.goalSelector.addGoal(2, new DragonMinionAttackGoal(shulker, 80));

		toRemove.clear();
		shulker.targetSelector.goals.forEach(goal -> {
			if (goal.getGoal() instanceof NearestAttackableTargetGoal)
				toRemove.add(goal.getGoal());
			if (goal.getGoal() instanceof HurtByTargetGoal)
				toRemove.add(goal.getGoal());
		});
		toRemove.forEach(shulker.targetSelector::removeGoal);

		shulker.targetSelector.addGoal(2, new PBNearestAttackableTargetGoal(shulker));
		shulker.targetSelector.addGoal(1, new HurtByTargetGoal(shulker, ShulkerEntity.class, EnderDragonEntity.class));
	}

	public ShulkerEntity summonMinion(World world, Vector3d pos, float difficulty) {
		ShulkerEntity shulker = new ShulkerEntity(EntityType.SHULKER, world);
		CompoundNBT minionTags = shulker.getPersistentData();
		minionTags.putBoolean(Strings.Tags.DRAGON_MINION, true);

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		boolean isBlindingMinion = world.getRandom().nextDouble() < this.blindingChance * difficulty;

		shulker.setPosition(pos.x, pos.y, pos.z);
		shulker.setCustomName(new TranslationTextComponent(Strings.Translatable.DRAGON_MINION));
		shulker.deathLootTable = LootTables.EMPTY;
		shulker.enablePersistence();
		DragonMinionHelper.setMinionColor(shulker, isBlindingMinion);

		ModifiableAttributeInstance followRange = shulker.getAttribute(Attributes.FOLLOW_RANGE);
		AttributeModifier followRangeBonus = new AttributeModifier(Strings.AttributeModifiers.FOLLOW_RANGE_BONUS_UUID, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS, 64, AttributeModifier.Operation.ADDITION);
		followRange.applyPersistentModifier(followRangeBonus);

		world.addEntity(shulker);
		return shulker;
	}

	@SubscribeEvent
	public void onMinionHurt(LivingAttackEvent event) {
		if (!this.isEnabled())
			return;

		if (!this.dragonImmune)
			return;

		if (!(event.getEntity() instanceof ShulkerEntity))
			return;

		ShulkerEntity shulker = (ShulkerEntity) event.getEntity();
		CompoundNBT compoundNBT = shulker.getPersistentData();
		if (!compoundNBT.contains(Strings.Tags.DRAGON_MINION))
			return;

		if (event.getSource().getTrueSource() instanceof EnderDragonEntity || event.getSource().getImmediateSource() instanceof EnderDragonEntity)
			event.setCanceled(true);
	}

	public void onBulletTick(ShulkerBulletEntity shulkerBulletEntity) {
		if (!shulkerBulletEntity.world.isRemote && shulkerBulletEntity.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET)) {
			((ServerWorld)shulkerBulletEntity.world).spawnParticle(ParticleTypes.ENTITY_EFFECT, shulkerBulletEntity.getPosX(), shulkerBulletEntity.getPosY(), shulkerBulletEntity.getPosZ(), 1, 0d, 0d, 0d, 0d);
		}
	}

	public void onBulletEntityHit(ShulkerBulletEntity shulkerBulletEntity, EntityRayTraceResult rayTraceResult) {
		Entity entityHit = rayTraceResult.getEntity();
		Entity entityOwner = shulkerBulletEntity.getShooter();
		LivingEntity livingEntityOwner = entityOwner instanceof LivingEntity ? (LivingEntity)entityOwner : null;
		boolean flag = entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(shulkerBulletEntity, livingEntityOwner).setProjectile(), 4.0F);
		if (flag) {
			shulkerBulletEntity.applyEnchantments(livingEntityOwner, entityHit);
			if (entityHit instanceof LivingEntity) {
				((LivingEntity)entityHit).addPotionEffect(new EffectInstance(Effects.LEVITATION, 200));
				if (shulkerBulletEntity.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET))
					((LivingEntity)entityHit).addPotionEffect(new EffectInstance(Effects.BLINDNESS, 150));
			}
		}
	}
}
