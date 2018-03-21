package net.insane96mcp.progressivebosses.events.entities;

import java.lang.reflect.Field;
import java.util.List;

import net.insane96mcp.progressivebosses.lib.Properties;
import net.insane96mcp.progressivebosses.lib.Utils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityWitherSkeleton;
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
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

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
		
		float spawnedCount = -Properties.Wither.General.normalWitherCount;
		for (EntityPlayerMP player : players) {
			NBTTagCompound playerTags = player.getEntityData();
			int c = playerTags.getInteger("progressivebosses:spawnedwithers");
			spawnedCount += c;
			playerTags.setInteger("progressivebosses:spawnedwithers", c + 1);
		}
		
		if (spawnedCount == 0)
			return;
		
		if (!Properties.Wither.General.sumSpawnedWither && spawnedCount > 0)
			spawnedCount /= players.size();
		
		SetHealth(wither, spawnedCount);
		SetArmor(wither, spawnedCount);
		SetExperience(wither, spawnedCount);
		
		tags.setFloat("progressivebosses:difficulty", spawnedCount);
		
		int cooldown = MathHelper.getInt(wither.getRNG(), Properties.Wither.Skeletons.spawnMinCooldown, Properties.Wither.Skeletons.spawnMaxCooldown);
		tags.setInteger("progressivebosses:skeletons_cooldown", Properties.Wither.Skeletons.spawnMinCooldown);
	}
	
	public static void SetExperience(EntityWither wither, float difficulty) {
		try {
			Field experienceValue = ReflectionHelper.findField(EntityLiving.class, "experienceValue", "field_70728_aV", "b_");
			int xp = 50 + (int) (50 * (Properties.Wither.Rewards.bonusExperience * difficulty / 100f));
			experienceValue.set(wither, xp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
	
	private static void SetHealth(EntityWither wither, float spawnedCount) {
		IAttributeInstance health = wither.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		if (spawnedCount < 0) {
			health.setBaseValue(health.getBaseValue() / -(spawnedCount - 1));
		}
		else {
			health.setBaseValue(health.getBaseValue() + (spawnedCount * Properties.Wither.Health.bonusPerSpawned));
		}
		wither.setHealth((float) health.getBaseValue());
	}
	
	public static void SetArmor(EntityWither wither, float killedCount) {
		IAttributeInstance attribute = wither.getEntityAttribute(SharedMonsterAttributes.ARMOR);
		float armor = killedCount * Properties.Wither.Armor.bonusPerSpawned;
		if (armor > Properties.Wither.Armor.maximum)
			armor = Properties.Wither.Armor.maximum;
		attribute.setBaseValue(armor);
	}
	
	public static void Heal(EntityWither wither, NBTTagCompound tags) {
		if (Properties.Wither.Health.maximumRegeneration == 0.0f)
			return;
		
		float maxHeal = Properties.Wither.Health.maximumRegeneration;
		
		if (wither.ticksExisted % 20 != 0)
			return;
		
		float difficulty = tags.getFloat("progressivebosses:difficulty");
		
		if (difficulty == 0)
			return;
		
		float health = wither.getHealth();
		float heal = difficulty / 10f * Properties.Wither.Health.regenerationRate;
		
		if (heal > maxHeal)
			heal = maxHeal;

		if (wither.getHealth() < wither.getMaxHealth() && wither.getHealth() > 0.0f)
			wither.setHealth(health + heal);
		
		//System.out.println(dragon.getHealth());
	}
	
	private static void SpawnSkeletons(EntityWither wither, World world) {
		NBTTagCompound tags = wither.getEntityData();
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
				if (i % Properties.Wither.Skeletons.spawnAt == 0) {
					EntityWitherSkeleton witherSkeleton = new EntityWitherSkeleton(world);
					float x = (float) (wither.posX + (world.rand.nextFloat() * 3f - 1.5f));
					float y = (float) (wither.posY + (world.rand.nextFloat()));
					float z = (float) (wither.posZ + (world.rand.nextFloat() * 3f - 1.5f));
					while (world.getBlockState(new BlockPos(x, y, z)).causesSuffocation()) {
						y++;
						if (y + 4 > wither.posY)
							break;
					}
					if (world.rand.nextFloat() < (Properties.Wither.Skeletons.spawnWithSword + difficulty) / 100f)
						witherSkeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
					
					IAttributeInstance armor = witherSkeleton.getEntityAttribute(SharedMonsterAttributes.ARMOR);
					int minArmor = Properties.Wither.Skeletons.minArmor;
					int maxArmor = Math.round(difficulty);
					if (maxArmor > Properties.Wither.Skeletons.maxArmor)
						maxArmor = Properties.Wither.Skeletons.maxArmor;
					armor.setBaseValue(MathHelper.getInt(world.rand, minArmor, maxArmor));
					
					IAttributeInstance speed = witherSkeleton.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
					float minSpeedMultiplier = 1.0f;
					float maxSpeedMultiplier = 1.25f;
					speed.setBaseValue(speed.getBaseValue() * (Utils.Math.getFloat(world.rand, minSpeedMultiplier, maxSpeedMultiplier)));
					
					witherSkeleton.setPosition(x, y, z);
					witherSkeleton.setCustomNameTag("Wither's Minion");
					
					try {
						Field deathLootTable = ReflectionHelper.findField(EntityLiving.class, "deathLootTable", "field_184659_bA", "bC");
						deathLootTable.set(witherSkeleton, new ResourceLocation("minecraft:empty"));
						
						Field experienceValue = ReflectionHelper.findField(EntityLiving.class, "experienceValue", "field_70728_aV", "b_");
						int xp = 1;
						experienceValue.set(witherSkeleton, xp);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					world.spawnEntity(witherSkeleton);
				}
			}
		}
	}
}
