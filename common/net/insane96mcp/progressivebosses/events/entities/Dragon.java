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
		
		if (!Properties.Dragon.General.sumKilledDragons && killedCount > 0)
			killedCount /= players.size();
		
		SetHealth(dragon, killedCount);
		SetArmor(dragon, killedCount);
		
		tags.setFloat("progressivebosses:difficulty", killedCount);
	}
	
	private static void SetHealth(EntityDragon dragon, float killedCount) {
		IAttributeInstance attribute = dragon.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		attribute.setBaseValue(attribute.getBaseValue() + (killedCount * Properties.Dragon.Health.bonusPerKilled));
		dragon.setHealth((float) attribute.getBaseValue());
	}
	
	private static void SetArmor(EntityDragon dragon, float killedCount) {
		IAttributeInstance attribute = dragon.getEntityAttribute(SharedMonsterAttributes.ARMOR);
		float armor = killedCount * Properties.Dragon.Armor.bonusPerKilled;
		if (armor > Properties.Dragon.Armor.maximum)
			armor = Properties.Dragon.Armor.maximum;
		attribute.setBaseValue(armor);
	}
	
	private static void SetExperience(LivingExperienceDropEvent event) {
		if (!(event.getEntityLiving() instanceof EntityDragon))
			return;
		
		EntityDragon dragon = (EntityDragon)event.getEntityLiving();
		
		NBTTagCompound tags = dragon.getEntityData();
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		int baseXp = event.getOriginalExperience();
		float increase = (baseXp * (Properties.Dragon.Rewards.bonusExperience * difficulty / 100f));
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
			playerTags.setInteger("progressivebosses:killeddragons", c + 1);
		}
	}
	
	private static void DropEgg(EntityDragon dragon, World world) {
		if(dragon.getFightManager() == null || !dragon.getFightManager().hasPreviouslyKilledDragon() || dragon.deathTicks != 100)
			return;
			
		NBTTagCompound tags = dragon.getEntityData();
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		
		float chance = Properties.Dragon.Rewards.eggDropPerKilled * difficulty;
		if (chance > Properties.Dragon.Rewards.eggDropMaximum)
			chance = Properties.Dragon.Rewards.eggDropMaximum;
		
		if (dragon.world.rand.nextFloat() < chance / 100f) {
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
		if (Properties.Dragon.Health.maximumRegeneration == 0.0f)
			return;
		
		if (dragon.ticksExisted % 20 != 0)
			return;
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		
		if (difficulty == 0)
			return;

		float maxHeal = Properties.Dragon.Health.maximumRegeneration;
		float heal = difficulty * Properties.Dragon.Health.regenPerKilled;
		
		if (heal > maxHeal)
			heal = maxHeal;
		
		float health = dragon.getHealth();

		if (dragon.getHealth() < dragon.getMaxHealth() && dragon.getHealth() > 0.0f)
            dragon.setHealth(health + heal);
	}
	
	private static void SpawnEndermites(EntityDragon dragon, World world) {
		NBTTagCompound tags = dragon.getEntityData();
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < Properties.Dragon.Endermites.spawnAt)
			return;
		
		int cooldown = tags.getInteger("progressivebosses:endermites_cooldown");
		if (cooldown > 0) {
			tags.setInteger("progressivebosses:endermites_cooldown", cooldown - 1);
		}
		else {
			int cooldownReduction = (int) (difficulty * Properties.Dragon.Endermites.spawnCooldownReduction);
			cooldown = MathHelper.getInt(world.rand, Properties.Dragon.Endermites.spawnMinCooldown - cooldownReduction, Properties.Dragon.Endermites.spawnMaxCooldown - cooldownReduction);
			tags.setInteger("progressivebosses:endermites_cooldown", cooldown);
			for (int i = 1; i <= difficulty; i++) {
				if (i / Properties.Dragon.Endermites.spawnAt > Properties.Dragon.Endermites.spawnMaxCount)
					break;
				
				if (i % Properties.Dragon.Endermites.spawnAt == 0) {
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
					
					try {
						Reflection.livingExperienceValue.set(endermite, 1);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					world.spawnEntity(endermite);
				}
			}
		}
	}

	private static void SpawnShulkers(EntityDragon dragon, World world) {
		NBTTagCompound tags = dragon.getEntityData();
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < Properties.Dragon.Shulkers.spawnAt)
			return;
		
		int cooldown = tags.getInteger("progressivebosses:shulkers_cooldown");
		if (cooldown > 0) {
			tags.setInteger("progressivebosses:shulkers_cooldown", cooldown - 1);
		}
		else {
			int cooldownReduction = (int) (difficulty * Properties.Dragon.Shulkers.spawnCooldownReduction);
			cooldown = MathHelper.getInt(world.rand, Properties.Dragon.Shulkers.spawnMinCooldown - cooldownReduction, Properties.Dragon.Shulkers.spawnMaxCooldown - cooldownReduction);
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
			
			try {
				Reflection.livingDeathLootTable.set(shulker, LootTables.dragonMinion);
				Reflection.livingExperienceValue.set(shulker, 2);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			world.spawnEntity(shulker);
		}
	}
}
