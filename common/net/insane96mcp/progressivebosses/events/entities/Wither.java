package net.insane96mcp.progressivebosses.events.entities;

import java.util.List;

import net.insane96mcp.progressivebosses.lib.Properties;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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

		int radius = Properties.Wither.spawnRadiusPlayerCheck;
		BlockPos pos1 = wither.getPosition().add(-radius, -radius, -radius);
		BlockPos pos2 = wither.getPosition().add(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
		
		List<EntityPlayerMP> players = event.getWorld().getEntitiesWithinAABB(EntityPlayerMP.class, bb);
		if (players.size() == 0)
			return;
		
		float spawnedCount = -Properties.Wither.normalWitherCount;
		for (EntityPlayerMP player : players) {
			NBTTagCompound playerTags = player.getEntityData();
			int c = playerTags.getInteger("progressivebosses:spawnedwithers");
			spawnedCount += c;
			playerTags.setInteger("progressivebosses:spawnedwithers", c + 1);
		}
		
		if (spawnedCount == 0)
			return;
		
		if (!Properties.Wither.sumSpawnedWither && spawnedCount > 0)
			spawnedCount /= players.size();
		
		SetHealth(wither, spawnedCount);
		
		
		tags.setInteger("progressivebosses:difficulty", (int) spawnedCount);
		tags.setInteger("progressivebosses:skeletons_cooldown", Properties.Wither.spawnWitherSkeletonsMinCooldown);
	}
	
	private static void SetHealth(EntityWither wither, float spawnedCount) {
		IAttributeInstance health = wither.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		if (spawnedCount < 0) {
			health.setBaseValue(health.getBaseValue() / -(spawnedCount - 1));
		}
		else {
			health.setBaseValue(health.getBaseValue() + (spawnedCount * Properties.Wither.bonusHealthPerSpawned));
		}
		wither.setHealth((float) health.getBaseValue());
	}
	
	public static void Update(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof EntityWither))
			return;
		
		World world = event.getEntity().world;
		
		EntityWither wither = (EntityWither)event.getEntity();
		if (wither.getInvulTime() > 0)
			return;
		
		SpawnSkeletons(wither, world);
	}
	
	private static void SpawnSkeletons(EntityWither wither, World world) {
		NBTTagCompound tags = wither.getEntityData();
		int difficulty = tags.getInteger("progressivebosses:difficulty");
		if (difficulty < Properties.Wither.spawnWitherSkeletonsAt)
			return;
		
		int cooldown = tags.getInteger("progressivebosses:skeletons_cooldown");
		if (cooldown > 0) {
			tags.setInteger("progressivebosses:skeletons_cooldown", cooldown - 1);
		}
		else {
			cooldown = MathHelper.getInt(world.rand, Properties.Wither.spawnWitherSkeletonsMinCooldown, Properties.Wither.spawnWitherSkeletonsMaxCooldown);
			tags.setInteger("progressivebosses:skeletons_cooldown", cooldown);
			for (int i = 1; i <= difficulty; i++) {
				if (i % Properties.Wither.spawnWitherSkeletonsAt == 0) {
					EntityWitherSkeleton witherSkeleton = new EntityWitherSkeleton(world);
					float x = (float) (wither.posX + (world.rand.nextFloat() * 3f - 1.5f));
					float y = (float) (wither.posY + (world.rand.nextFloat()));
					float z = (float) (wither.posZ + (world.rand.nextFloat() * 3f - 1.5f));
					while (world.getBlockState(new BlockPos(x, y, z)).causesSuffocation()) {
						y++;
						if (y + 4 > wither.posY)
							break;
					}
					if (world.rand.nextFloat() < (Properties.Wither.spawnWitherSkeletonsSword + difficulty) / 100f)
						witherSkeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
					witherSkeleton.setPosition(x, y, z);
					world.spawnEntity(witherSkeleton);
				}
			}
		}
	}
}
