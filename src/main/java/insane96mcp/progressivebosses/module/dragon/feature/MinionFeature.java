package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.ai.DragonMinionAttackGoal;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import insane96mcp.progressivebosses.utils.DragonMinionHelper;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Minions", description = "Shulkers that will make you float around.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon")
public class MinionFeature extends Feature {

	@Config(min = 0)
	@Label(name = "Minion at Difficulty", description = "At which difficulty the Ender Dragon starts spawning Minions")
	public static Integer minionAtDifficulty = 1;
	@Config(min = 0)
	@Label(name = "Minimum Cooldown", description = "Minimum ticks (20 ticks = 1 seconds) after Minions can spawn")
	public static Integer minCooldown = 1400;
	@Config(min = 0)
	@Label(name = "Maximum Cooldown", description = "Maximum ticks (20 ticks = 1 seconds) after Minions can spawn.")
	public static Integer maxCooldown = 2000;
	@Config(min = 0d, max = 1d)
	@Label(name = "Cooldown Reduction", description = "Percentage cooldown reduction at max difficulty for the cooldown of Minion spawning.")
	public static Double cooldownReduction = 0.40d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Blinding Chance", description = "Percentage chance at max difficulty for a Minion to spawn as a Blinding Minion.")
	public static Double blindingChance = 0.40d;
	@Config(min = 0, max = 1200)
	@Label(name = "Blinding duration", description = "Time (in ticks) for the blinding effect when hit by a blinding bullet.")
	public static Integer blindingDuration = 150;
	@Config(min = 0)
	@Label(name = "Reduced Dragon Damage", description = "If true, Dragon Minions will take only 10% damage from the Ender Dragon.")
	public static Boolean reducedDragonDamage = true;

	public MinionFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
		if (minCooldown > maxCooldown)
			minCooldown = maxCooldown;
	}

	@SubscribeEvent
	public void onDragonSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon))
			return;

		CompoundTag dragonTags = dragon.getPersistentData();

		int cooldown = (int) (Mth.nextInt(dragon.getRandom(), minCooldown, maxCooldown) * 0.5d);
		dragonTags.putInt(Strings.Tags.DRAGON_MINION_COOLDOWN, cooldown);
	}

	@SubscribeEvent
	public void onShulkerSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof Shulker shulker))
			return;

		CompoundTag tags = shulker.getPersistentData();
		if (!tags.contains(Strings.Tags.DRAGON_MINION))
			return;

		setMinionAI(shulker);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon))
			return;

		Level world = event.getEntity().level;

		CompoundTag dragonTags = dragon.getPersistentData();

		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);
		if (difficulty < minionAtDifficulty
				|| dragon.getHealth() <= 0) return;

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

		cooldown = Mth.nextInt(world.random, minCooldown, maxCooldown);
		cooldown *= 1 - cooldownReduction * DifficultyHelper.getScalingDifficulty(dragon);
		dragonTags.putInt(Strings.Tags.DRAGON_MINION_COOLDOWN, cooldown - 1);

		float angle = world.random.nextFloat() * (float) Math.PI * 2f;
		float x = (float) (Math.cos(angle) * (Mth.nextFloat(dragon.getRandom(), 16f, 45f)));
		float z = (float) (Math.sin(angle) * (Mth.nextFloat(dragon.getRandom(), 16f, 45f)));
		float y = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 255, z)).getY();
		summonMinion(world, new Vec3(x, y, z), DifficultyHelper.getScalingDifficulty(dragon));
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

	public static void summonMinion(Level world, Vec3 pos, float scalingDifficulty) {
		Shulker shulker = EntityType.SHULKER.create(world);
		if (shulker == null) {
			LogHelper.warn("Failed to summon Dragon Minion");
			return;
		}
		CompoundTag minionTags = shulker.getPersistentData();
		minionTags.putBoolean(Strings.Tags.DRAGON_MINION, true);

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		boolean isBlindingMinion = world.getRandom().nextDouble() < blindingChance * scalingDifficulty;

		shulker.setPos(pos.x, pos.y, pos.z);
		shulker.setCustomName(Component.translatable(Strings.Translatable.DRAGON_MINION));
		shulker.lootTable = BuiltInLootTables.EMPTY;
		shulker.setPersistenceRequired();
		DragonMinionHelper.setMinionColor(shulker, isBlindingMinion);

		MCUtils.applyModifier(shulker, Attributes.FOLLOW_RANGE, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS_UUID, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS, 64, AttributeModifier.Operation.ADDITION);

		world.addFreshEntity(shulker);
	}

	@SubscribeEvent
	public void onMinionHurt(LivingHurtEvent event) {
		if (!this.isEnabled()
				|| !reducedDragonDamage
				|| !(event.getEntity() instanceof Shulker shulker))
			return;

		CompoundTag compoundNBT = shulker.getPersistentData();
		if (!compoundNBT.contains(Strings.Tags.DRAGON_MINION))
			return;

		if (event.getSource().getEntity() instanceof EnderDragon)
			event.setAmount(event.getAmount() * 0.1f);
	}

	public static void onBulletTick(ShulkerBullet shulkerBulletEntity) {
		if (!shulkerBulletEntity.level.isClientSide && shulkerBulletEntity.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET)) {
			((ServerLevel)shulkerBulletEntity.level).sendParticles(ParticleTypes.ENTITY_EFFECT, shulkerBulletEntity.getX(), shulkerBulletEntity.getY(), shulkerBulletEntity.getZ(), 1, 0d, 0d, 0d, 0d);
		}
	}
}
