package net.insane96mcp.progressivebosses.events.entities;

import java.util.List;

import net.insane96mcp.progressivebosses.item.ModItems;
import net.insane96mcp.progressivebosses.lib.Properties;
import net.insane96mcp.progressivebosses.lib.Reflection;
import net.insane96mcp.progressivebosses.lib.Utils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class Wither {
	public static void SetStats(EntityJoinWorldEvent event) {
		if (!(event.getEntity() instanceof EntityWither))
			return;
		
		EntityWither wither = (EntityWither)event.getEntity();
		
		NBTTagCompound tags = wither.getEntityData();
		boolean alreadySpawned = tags.getBoolean("progressivebosses:spawned");
		
		if (alreadySpawned)
			return;
		
		tags.setBoolean("progressivebosses:spawned", true);

		int radius = Properties.config.wither.general.spawnRadiusPlayerCheck;
		BlockPos pos1 = wither.getPosition().add(-radius, -radius, -radius);
		BlockPos pos2 = wither.getPosition().add(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
		
		List<EntityPlayerMP> players = event.getWorld().getEntitiesWithinAABB(EntityPlayerMP.class, bb);
		if (players.size() == 0)
			return;
		
		float spawnedCount = 1;
		for (EntityPlayerMP player : players) {
			NBTTagCompound playerTags = player.getEntityData();
			int c = playerTags.getInteger("progressivebosses:spawnedwithers");
			spawnedCount += c;
			if (c >= Properties.config.wither.general.maxDifficulty)
				continue;
			playerTags.setInteger("progressivebosses:spawnedwithers", c + 1);
		}
		
		if (spawnedCount == 1)
			return;
		
		if (!Properties.config.wither.general.sumSpawnedWither)
			spawnedCount /= players.size();
		
		SetHealth(wither, spawnedCount);
		SetArmor(wither, spawnedCount);
		SetExperience(wither, spawnedCount);
		
		tags.setFloat("progressivebosses:difficulty", spawnedCount);
		
		int cooldown = MathHelper.getInt(wither.getRNG(), Properties.config.wither.minions.spawnMinCooldown, Properties.config.wither.minions.spawnMaxCooldown);
		tags.setInteger("progressivebosses:skeletons_cooldown", Properties.config.wither.minions.spawnMinCooldown);
	}
	
	private static void SetExperience(EntityWither wither, float difficulty) {
		int xp = 50 + (int) (50 * (Properties.config.wither.rewards.bonusExperience * difficulty / 100f));
		Reflection.Set(Reflection.livingExperienceValue, wither, xp);
	}
	
	private static void SetHealth(EntityWither wither, float spawnedCount) {
		IAttributeInstance health = wither.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		if (spawnedCount < 0) {
			health.setBaseValue(health.getBaseValue() / -(spawnedCount - 1));
		}
		else {
			health.setBaseValue(health.getBaseValue() + (spawnedCount * Properties.config.wither.health.bonusPerSpawned));
		}
		wither.setHealth(Math.max(1, (float) health.getBaseValue() - 200));
	}
	
	private static void SetArmor(EntityWither wither, float killedCount) {
		IAttributeInstance attribute = wither.getEntityAttribute(SharedMonsterAttributes.ARMOR);
		float armor = killedCount * Properties.config.wither.armor.bonusPerSpawned;
		if (armor > Properties.config.wither.armor.maximum)
			armor = Properties.config.wither.armor.maximum;
		attribute.setBaseValue(armor);
	}
	
	
	public static void Update(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof EntityWither))
			return;
		
		World world = event.getEntity().world;
		
		EntityWither wither = (EntityWither)event.getEntity();
		NBTTagCompound tags = wither.getEntityData();

		if (wither.getInvulTime() > 0) {
			Reflection.Invoke(Reflection.bossInfoServerSetPercent, Reflection.Get(Reflection.witherBossInfo, wither), wither.getHealth() / wither.getMaxHealth());
		}
		else {
			SpawnSkeletons(wither, world);
			Heal(wither, tags);
		}
	}
	
	private static void Heal(EntityWither wither, NBTTagCompound tags) {
		if (Properties.config.wither.health.maximumRegeneration == 0.0f)
			return;
		
		if (wither.ticksExisted % 20 != 0)
			return;
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		
		if (difficulty <= 0)
			return;
		
		float maxHeal = Properties.config.wither.health.maximumRegeneration;
		float heal = difficulty * Properties.config.wither.health.regenPerSpawned;
		
		if (heal > maxHeal)
			heal = maxHeal;
		
		float health = wither.getHealth();

		if (wither.getHealth() < wither.getMaxHealth() && wither.getHealth() > 0.0f)
			wither.setHealth(health + heal);
	}
	
	private static void SpawnSkeletons(EntityWither wither, World world) {
		if (Properties.config.wither.minions.spawnAfter < 0)
			return;
		
		int radius = 24;
		BlockPos pos1 = wither.getPosition().add(-radius, -radius, -radius);
		BlockPos pos2 = wither.getPosition().add(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, bb);
		
		if (players.isEmpty())
			return;
		
		List<EntityWitherSkeleton> minions = world.getEntitiesWithinAABB(EntityWitherSkeleton.class, bb);
		int minionsCount = minions.size();
		
		if (minionsCount >= Properties.config.wither.minions.maxMinions && Properties.config.wither.minions.maxMinions > 0)
			return;
		
		NBTTagCompound tags = wither.getEntityData();
		
		//Mobs Properties Randomness
		tags.setBoolean("mobsrandomizzation:preventProcessing", true);
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < Properties.config.wither.minions.spawnAfter)
			return;
		
		int cooldown = tags.getInteger("progressivebosses:skeletons_cooldown");
		if (cooldown > 0) {
			tags.setInteger("progressivebosses:skeletons_cooldown", cooldown - 1);
		}
		else { 
			cooldown = MathHelper.getInt(world.rand, Properties.config.wither.minions.spawnMinCooldown, Properties.config.wither.minions.spawnMaxCooldown);
			tags.setInteger("progressivebosses:skeletons_cooldown", cooldown);
			for (int i = Properties.config.wither.minions.spawnAfter; i <= difficulty; i++) {
				if (minionsCount >= Properties.config.wither.minions.maxMinions && Properties.config.wither.minions.maxMinions > 0)
					return;
				
				int spawn = i - Properties.config.wither.minions.spawnAfter;
				
				//Stops spawning if max count has reached
				if (spawn / Properties.config.wither.minions.spawnEvery >= Properties.config.wither.minions.spawnMaxCount)
					break;
				
				if (spawn % Properties.config.wither.minions.spawnEvery == 0) {
					EntityWitherSkeleton witherSkeleton = new EntityWitherSkeleton(world);
					int x = (int) (wither.posX + (MathHelper.getInt(world.rand, -2, 2)));
					int y = (int) (wither.posY - 2);
					int z = (int) (wither.posZ + (MathHelper.getInt(world.rand, -2, 2)));
					
					boolean shouldSpawn = true;
					while (world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()) {
						y++;
						if (y > wither.posY + 4) {
							shouldSpawn = false;
							break;
						}
					}
					if (!shouldSpawn)
						continue;
					
					IAttributeInstance armor = witherSkeleton.getEntityAttribute(SharedMonsterAttributes.ARMOR);
					float minArmor = Properties.config.wither.minions.minArmor;
					float maxArmor = Properties.config.wither.minions.maxArmor;
					armor.setBaseValue(Utils.Math.getFloat(world.rand, minArmor, maxArmor));
					
					IAttributeInstance speed = witherSkeleton.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
					float maxSpeedMultiplier = 1.25f;
					float speedMultiplier = difficulty / 100f + 1f;
					if (speedMultiplier > maxSpeedMultiplier)
						speedMultiplier = maxSpeedMultiplier;
					speed.setBaseValue(speed.getBaseValue() * speedMultiplier);
					
					witherSkeleton.setPosition(x + 0.5f, y + 0.5f, z + 0.5f);
					witherSkeleton.setCustomNameTag("Wither's Minion");
					Reflection.Set(Reflection.livingDeathLootTable, witherSkeleton, new ResourceLocation("minecraft:empty"));
					Reflection.Set(Reflection.livingExperienceValue, witherSkeleton, 1);
					
					world.spawnEntity(witherSkeleton);
					
					minionsCount++;
				}
			}
		}
	}

	
	public static void SetDrops(LivingDropsEvent event) {
		if (!(event.getEntityLiving() instanceof EntityWither))
			return;
		
		EntityWither wither = (EntityWither)event.getEntityLiving();
		
		NBTTagCompound tags = wither.getEntityData();
		float difficulty = tags.getFloat("progressivebosses:difficulty");

		float chance = Properties.config.wither.rewards.shardPerSpawned * difficulty;
		if (chance > Properties.config.wither.rewards.shardMaxChance)
			chance = Properties.config.wither.rewards.shardMaxChance;

		int tries = (int) (difficulty / Properties.config.wither.rewards.shardDivider) + 1;
		if (tries > Properties.config.wither.rewards.shardMaxCount)
			tries = Properties.config.wither.rewards.shardMaxCount;
		int count = 0;
		for (int i = 0; i < tries; i++) {
			if (wither.world.rand.nextFloat() >= chance / 100f)
				continue;
			count++;
		}
		if (count == 0)
			return;
		
		EntityItem shard = new EntityItem(wither.world, wither.posX, wither.posY, wither.posZ, new ItemStack(ModItems.starShard, count));
		
		event.getDrops().add(shard);
	}
}
