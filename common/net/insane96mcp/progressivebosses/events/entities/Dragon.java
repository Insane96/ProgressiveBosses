package net.insane96mcp.progressivebosses.events.entities;

import java.util.List;

import net.insane96mcp.progressivebosses.lib.LootTables;
import net.insane96mcp.progressivebosses.lib.Properties;
import net.insane96mcp.progressivebosses.lib.Reflection;
import net.insane96mcp.progressivebosses.lib.Utils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;

public class Dragon {
	public static void SetStats(EntityJoinWorldEvent event) {
		if (event.getWorld().provider.getDimension() != 1)
			return;
		
		if (!(event.getEntity() instanceof EntityDragon))
			return;
		
		EntityDragon dragon = (EntityDragon)event.getEntity();
		
		NBTTagCompound tags = dragon.getEntityData();
		boolean alreadySpawned = tags.getBoolean("progressivebosses:spawned");
		
		if (alreadySpawned)
			return;
		
		tags.setBoolean("progressivebosses:spawned", true);

		int radius = 160;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
				
		List<EntityPlayerMP> players = event.getWorld().getEntitiesWithinAABB(EntityPlayerMP.class, bb);
		if (players.size() == 0)
			return;
		
		float killedCount = 0;
		for (EntityPlayerMP player : players) {
			NBTTagCompound playerTags = player.getEntityData();
			int c = playerTags.getInteger("progressivebosses:killeddragons");
			killedCount += c;
		}
		
		if (killedCount == 0)
			return;
		
		if (!Properties.config.dragon.general.sumKilledDragons && killedCount > 0)
			killedCount /= players.size();
		
		SetHealth(dragon, killedCount);
		SetArmor(dragon, killedCount);
		
		tags.setFloat("progressivebosses:difficulty", killedCount);
	}
	
	private static void SetHealth(EntityDragon dragon, float killedCount) {
		IAttributeInstance attribute = dragon.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		attribute.setBaseValue(attribute.getBaseValue() + (killedCount * Properties.config.dragon.health.bonusPerKilled));
		dragon.setHealth((float) attribute.getBaseValue());
	}
	
	private static void SetArmor(EntityDragon dragon, float killedCount) {
		IAttributeInstance attribute = dragon.getEntityAttribute(SharedMonsterAttributes.ARMOR);
		float armor = killedCount * Properties.config.dragon.armor.bonusPerKilled;
		if (armor > Properties.config.dragon.armor.maximum)
			armor = Properties.config.dragon.armor.maximum;
		attribute.setBaseValue(armor);
	}
	
	private static void SetExperience(LivingExperienceDropEvent event) {
		if (!(event.getEntityLiving() instanceof EntityDragon))
			return;
		
		EntityDragon dragon = (EntityDragon)event.getEntityLiving();
		
		NBTTagCompound tags = dragon.getEntityData();
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		int baseXp = event.getOriginalExperience();
		float increase = (baseXp * (Properties.config.dragon.rewards.bonusExperience * difficulty / 100f));
		event.setDroppedExperience((int) (baseXp + increase));
	}
	
	
	public static void OnDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof EntityDragon))
			return;
		
		EntityDragon dragon = (EntityDragon)event.getEntity();
		NBTTagCompound tags = dragon.getEntityData();
		if (tags.getBoolean("progressivebosses:hasbeenkilled"))
			return;
		tags.setBoolean("progressivebosses:hasbeenkilled", true);

		int radius = 160;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
				
		List<EntityPlayerMP> players = dragon.world.getEntitiesWithinAABB(EntityPlayerMP.class, bb);
		if (players.size() == 0)
			return;
		
		int c = 0;
		for (EntityPlayerMP player : players) {
			NBTTagCompound playerTags = player.getEntityData();
			c = playerTags.getInteger("progressivebosses:killeddragons");
			if (c >= Properties.config.wither.general.maxDifficulty)
				continue;
			playerTags.setInteger("progressivebosses:killeddragons", c + 1);
		}
	}
	
	private static void DropEgg(EntityDragon dragon, World world) {
		if(dragon.getFightManager() == null || dragon.deathTicks != 100)
			return;
			
		NBTTagCompound tags = dragon.getEntityData();
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		
		if (difficulty < 1.0f) {
			world.setBlockState(new BlockPos(0, 255, 0), Blocks.DRAGON_EGG.getDefaultState());
		}
	}

	
	public static void Update(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof EntityDragon))
			return;
		
		World world = event.getEntity().world;
		
		EntityDragon dragon = (EntityDragon)event.getEntity();
		NBTTagCompound tags = dragon.getEntityData();
		
		SpawnEndermites(dragon, world);
		SpawnShulkers(dragon, world);
		Heal(dragon, tags);
		DropEgg(dragon, world);
	}
	
	private static void Heal(EntityDragon dragon, NBTTagCompound tags) {
		if (Properties.config.dragon.health.maximumRegeneration == 0.0f)
			return;
		
		if (dragon.ticksExisted % 20 != 0)
			return;
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		
		if (difficulty == 0)
			return;

		float maxHeal = Properties.config.dragon.health.maximumRegeneration;
		float heal = difficulty * Properties.config.dragon.health.regenPerKilled;
		
		if (heal > maxHeal)
			heal = maxHeal;
		
		float health = dragon.getHealth();

		if (dragon.getHealth() < dragon.getMaxHealth() && dragon.getHealth() > 0.0f)
            dragon.setHealth(health + heal);
	}
	
	private static void SpawnEndermites(EntityDragon dragon, World world) {
		if (Properties.config.dragon.endermites.spawnEvery <= 0)
			return;
		
		NBTTagCompound tags = dragon.getEntityData();
		
		//Mobs Properties Randomness
		tags.setBoolean("mobsrandomizzation:preventProcessing", true);
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < Properties.config.dragon.endermites.spawnEvery)
			return;
		
		int cooldown = tags.getInteger("progressivebosses:endermites_cooldown");
		if (cooldown > 0) {
			tags.setInteger("progressivebosses:endermites_cooldown", cooldown - 1);
		}
		else {
			int cooldownReduction = (int) (difficulty * Properties.config.dragon.endermites.spawnCooldownReduction);
			cooldown = MathHelper.getInt(world.rand, Properties.config.dragon.endermites.spawnMinCooldown - cooldownReduction, Properties.config.dragon.endermites.spawnMaxCooldown - cooldownReduction);
			tags.setInteger("progressivebosses:endermites_cooldown", cooldown);
			for (int i = 1; i <= difficulty; i++) {
				if (i / Properties.config.dragon.endermites.spawnEvery > Properties.config.dragon.endermites.spawnEvery)
					break;
				
				if (i % Properties.config.dragon.endermites.spawnEvery == 0) {
					EntityEndermite endermite = new EntityEndermite(world);
					float angle = world.rand.nextFloat() * (float) Math.PI * 2f;
					float x = (float) (Math.cos(angle) * 3.15f);
					float z = (float) (Math.sin(angle) * 3.15f);
					BlockPos getY = world.getTopSolidOrLiquidBlock(new BlockPos(0, 255, 0));
					int bedrockCounter = 0;
					float y = 64;
					for (int yLevel = getY.getY(); yLevel > 0; yLevel--) {
						if (world.getBlockState(new BlockPos(0, yLevel, 0)).getBlock().equals(Blocks.BEDROCK))
							bedrockCounter++;
						
						if (bedrockCounter == 3) {
							y = yLevel;
							break;
						}
					}
					IAttributeInstance instance = endermite.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
					instance.setBaseValue(instance.getBaseValue() * 1.55f);
					instance = endermite.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
					instance.setBaseValue(64f);
					endermite.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("resistance"), 80, 4, true, true));
					endermite.setPosition(x, y, z);
					endermite.setCustomNameTag("Dragon's Larva");
					
					Reflection.Set(Reflection.livingExperienceValue, endermite, 1);
					
					world.spawnEntity(endermite);
				}
			}
		}
	}

	private static void SpawnShulkers(EntityDragon dragon, World world) {
		if (Properties.config.dragon.shulkers.spawnAfter <= 0)
			return;
		
		NBTTagCompound tags = dragon.getEntityData();
		
		//Mobs Properties Randomness
		tags.setBoolean("mobsrandomizzation:preventProcessing", true);
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < Properties.config.dragon.shulkers.spawnAfter)
			return;
		
		int cooldown = tags.getInteger("progressivebosses:shulkers_cooldown");
		if (cooldown > 0) {
			tags.setInteger("progressivebosses:shulkers_cooldown", cooldown - 1);
		}
		else {
			int cooldownReduction = (int) (difficulty * Properties.config.dragon.shulkers.spawnCooldownReduction);
			cooldown = MathHelper.getInt(world.rand, Properties.config.dragon.shulkers.spawnMinCooldown - cooldownReduction, Properties.config.dragon.shulkers.spawnMaxCooldown - cooldownReduction);
			tags.setInteger("progressivebosses:shulkers_cooldown", cooldown);
			
			EntityShulker shulker = new EntityShulker(world);
			float angle = world.rand.nextFloat() * (float) Math.PI * 2f;
			float x = (float) (Math.cos(angle) * (Utils.Math.getFloat(world.rand, 15f, 25f)));
			float y = 68;
			float z = (float) (Math.sin(angle) * (Utils.Math.getFloat(world.rand, 15f, 25f)));
			IAttributeInstance followRange = shulker.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
			followRange.setBaseValue(64f);
			shulker.setPosition(x, y, z);
			shulker.setCustomNameTag("Dragon's Minion");
			
			Reflection.Set(Reflection.livingDeathLootTable, shulker, LootTables.dragonMinion);
			Reflection.Set(Reflection.livingExperienceValue, shulker, 2);
			
			world.spawnEntity(shulker);
		}
	}
}
