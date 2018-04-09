package net.insane96mcp.progressivebosses.events.entities;

import java.util.List;

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
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
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

		int radius = Properties.Wither.General.spawnRadiusPlayerCheck;
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
			playerTags.setInteger("progressivebosses:spawnedwithers", c + 1);
		}
		
		if (spawnedCount == 1)
			return;
		
		if (!Properties.Wither.General.sumSpawnedWither)
			spawnedCount /= players.size();
		
		SetHealth(wither, spawnedCount);
		SetArmor(wither, spawnedCount);
		SetExperience(wither, spawnedCount);
		
		tags.setFloat("progressivebosses:difficulty", spawnedCount);
		
		int cooldown = MathHelper.getInt(wither.getRNG(), Properties.Wither.Skeletons.spawnMinCooldown, Properties.Wither.Skeletons.spawnMaxCooldown);
		tags.setInteger("progressivebosses:skeletons_cooldown", Properties.Wither.Skeletons.spawnMinCooldown);
	}
	
	private static void SetExperience(EntityWither wither, float difficulty) {
		try {
			int xp = 50 + (int) (50 * (Properties.Wither.Rewards.bonusExperience * difficulty / 100f));
			Reflection.livingExperienceValue.set(wither, xp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void SetHealth(EntityWither wither, float spawnedCount) {
		IAttributeInstance health = wither.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		if (spawnedCount < 0) {
			health.setBaseValue(health.getBaseValue() / -(spawnedCount - 1));
		}
		else {
			health.setBaseValue(health.getBaseValue() + (spawnedCount * Properties.Wither.Health.bonusPerSpawned));
		}
		wither.setHealth(Math.max(1, (float) health.getBaseValue() - 200));
	}
	
	private static void SetArmor(EntityWither wither, float killedCount) {
		IAttributeInstance attribute = wither.getEntityAttribute(SharedMonsterAttributes.ARMOR);
		float armor = killedCount * Properties.Wither.Armor.bonusPerSpawned;
		if (armor > Properties.Wither.Armor.maximum)
			armor = Properties.Wither.Armor.maximum;
		attribute.setBaseValue(armor);
	}
	
	
	public static void Update(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof EntityWither))
			return;
		
		World world = event.getEntity().world;
		
		EntityWither wither = (EntityWither)event.getEntity();
		NBTTagCompound tags = wither.getEntityData();
		
		if (wither.getInvulTime() > 0)
			return;
		
		SpawnSkeletons(wither, world);
		Heal(wither, tags);
	}
	
	private static void Heal(EntityWither wither, NBTTagCompound tags) {
		if (Properties.Wither.Health.maximumRegeneration == 0.0f)
			return;
		
		if (wither.ticksExisted % 20 != 0)
			return;
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		
		if (difficulty <= 0)
			return;
		
		float maxHeal = Properties.Wither.Health.maximumRegeneration;
		float heal = difficulty * Properties.Wither.Health.regenPerSpawned;
		
		if (heal > maxHeal)
			heal = maxHeal;
		
		float health = wither.getHealth();

		if (wither.getHealth() < wither.getMaxHealth() && wither.getHealth() > 0.0f)
			wither.setHealth(health + heal);
	}
	
	private static void SpawnSkeletons(EntityWither wither, World world) {
		int radius = 24;
		BlockPos pos1 = wither.getPosition().add(-radius, -radius, -radius);
		BlockPos pos2 = wither.getPosition().add(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, bb);
		
		if (players.isEmpty())
			return;
		
		NBTTagCompound tags = wither.getEntityData();
		
		//Mobs Properties Randomness
		tags.setBoolean("mobsrandomizzation:preventProcessing", true);
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < Properties.Wither.Skeletons.spawnAt)
			return;
		
		int cooldown = tags.getInteger("progressivebosses:skeletons_cooldown");
		if (cooldown > 0) {
			tags.setInteger("progressivebosses:skeletons_cooldown", cooldown - 1);
		}
		else {
			cooldown = MathHelper.getInt(world.rand, Properties.Wither.Skeletons.spawnMinCooldown, Properties.Wither.Skeletons.spawnMaxCooldown);
			tags.setInteger("progressivebosses:skeletons_cooldown", cooldown);
			for (int i = 1; i <= difficulty; i++) {
				if (i / Properties.Wither.Skeletons.spawnAt > Properties.Wither.Skeletons.spawnMaxCount)
					break;
				
				if (i % Properties.Wither.Skeletons.spawnAt == 0) {
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
					
					if (world.rand.nextFloat() < (Properties.Wither.Skeletons.spawnWithSword + difficulty) / 100f)
						witherSkeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
					witherSkeleton.setDropChance(EntityEquipmentSlot.MAINHAND, Float.MIN_VALUE);
					IAttributeInstance armor = witherSkeleton.getEntityAttribute(SharedMonsterAttributes.ARMOR);
					float minArmor = Properties.Wither.Skeletons.minArmor;
					float maxArmor = difficulty;
					if (maxArmor > Properties.Wither.Skeletons.maxArmor)
						maxArmor = Properties.Wither.Skeletons.maxArmor;
					armor.setBaseValue(Utils.Math.getFloat(world.rand, minArmor, maxArmor));
					
					IAttributeInstance speed = witherSkeleton.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
					float maxSpeedMultiplier = 1.25f;
					float speedMultiplier = difficulty / 100f + 1f;
					if (speedMultiplier > maxSpeedMultiplier)
						speedMultiplier = maxSpeedMultiplier;
					speed.setBaseValue(speed.getBaseValue() * speedMultiplier);
					
					witherSkeleton.setPosition(x + 0.5f, y + 0.5f, z + 0.5f);
					witherSkeleton.setCustomNameTag("Wither's Minion");
					try {
						Reflection.livingDeathLootTable.set(witherSkeleton, new ResourceLocation("minecraft:empty"));
						
						Reflection.livingExperienceValue.set(witherSkeleton, 1);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					world.spawnEntity(witherSkeleton);
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
		
		float chance = Properties.Wither.Rewards.skullPerSpawned * difficulty;
		if (chance > Properties.Wither.Rewards.skullMaxChance)
			chance = Properties.Wither.Rewards.skullMaxChance;
		if (wither.world.rand.nextFloat() >= chance / 100f)
			return;
		
		EntityItem skull = new EntityItem(wither.world, wither.posX, wither.posY, wither.posZ, new ItemStack(Items.SKULL, 1, 1));
		
		event.getDrops().add(skull);
	}
}
