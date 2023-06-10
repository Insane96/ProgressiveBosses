package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.entity.Larva;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Label(name = "Larva", description = "Mini things that are just annoying.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon")
public class LarvaFeature extends Feature {

	@Config(min = 0)
	@Label(name = "Larva at Difficulty", description = "At which difficulty the Ender Dragon starts spawning Larvae")
	public static Integer larvaAtDifficulty = 1;
	@Config(min = 0)
	@Label(name = "Bonus Larva Every Difficulty", description = "As the Ender Dragon starts spawning Minions, every how much difficulty she will spawn one more Minions")
	public static Integer bonusLarvaEveryDifficulty = 1;
	@Config(min = 0)
	@Label(name = "Max Larvae Spawned", description = "Maximum Larva spawned by the Ender Dragon")
	public static Integer maxSpawned = 7;
	@Config(min = 0)
	@Label(name = "Minimum Cooldown", description = "Minimum ticks (20 ticks = 1 seconds) after Minions can spawn.")
	public static Integer minCooldown = 800;
	@Config(min = 0)
	@Label(name = "Maximum Cooldown", description = "Maximum ticks (20 ticks = 1 seconds) after Minions can spawn.")
	public static Integer maxCooldown = 1400;
	@Config
	@Label(name = "Reduced Dragon Damage", description = "If true, Larvae will take only 10% damage from the Ender Dragon.")
	public static Boolean reducedDragonDamage = true;

	public LarvaFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(ModConfigEvent event) {
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

		//The first cooldown is halved
		int cooldown = (int) (Mth.nextInt(dragon.getRandom(), minCooldown, maxCooldown) * 0.5d);
		dragonTags.putInt(Strings.Tags.DRAGON_LARVA_COOLDOWN, cooldown);
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
		if (difficulty < larvaAtDifficulty)
			return;

		if (dragon.getHealth() <= 0)
			return;

		int cooldown = dragonTags.getInt(Strings.Tags.DRAGON_LARVA_COOLDOWN);
		if (cooldown > 0) {
			dragonTags.putInt(Strings.Tags.DRAGON_LARVA_COOLDOWN, cooldown - 1);
			return;
		}

		//If there is no player in the main island don't spawn larvae
		BlockPos centerPodium = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AABB bb = new AABB(centerPodium).inflate(64d);
		List<ServerPlayer> players = world.getEntitiesOfClass(ServerPlayer.class, bb);

		if (players.isEmpty())
			return;

		cooldown = Mth.nextInt(world.random, minCooldown, maxCooldown);
		dragonTags.putInt(Strings.Tags.DRAGON_LARVA_COOLDOWN, cooldown - 1);

		int larvaSpawnedCount = 0;
		for (int i = larvaAtDifficulty; i <= difficulty; i += bonusLarvaEveryDifficulty) {
			float angle = world.random.nextFloat() * (float) Math.PI * 2f;
			float x = (float) Math.floor(Math.cos(angle) * 3.33f);
			float z = (float) Math.floor(Math.sin(angle) * 3.33f);
			int y = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 255, z)).getY();
			summonLarva(world, new Vec3(x + 0.5, y, z + 0.5), difficulty);
			larvaSpawnedCount++;
			if (larvaSpawnedCount >= maxSpawned)
				break;
		}
	}

	public static void summonLarva(Level world, Vec3 pos, float difficulty) {
		Larva larva = new Larva(PBEntities.LARVA.get(), world);
		CompoundTag minionTags = larva.getPersistentData();

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		larva.setPos(pos.x, pos.y, pos.z);
		larva.setPersistenceRequired();

		MCUtils.applyModifier(larva, Attributes.ATTACK_DAMAGE, Strings.AttributeModifiers.ATTACK_DAMAGE_BONUS_UUID, Strings.AttributeModifiers.ATTACK_DAMAGE_BONUS, 0.35 * difficulty, AttributeModifier.Operation.ADDITION);
		MCUtils.applyModifier(larva, ForgeMod.SWIM_SPEED.get(), Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2.5d, AttributeModifier.Operation.MULTIPLY_BASE);

		world.addFreshEntity(larva);
	}

	public static boolean shouldTakeReducedDamage(DamageSource damageSource) {
		return damageSource.getEntity() instanceof EnderDragon && isEnabled(LarvaFeature.class) && reducedDragonDamage;
	}
}
