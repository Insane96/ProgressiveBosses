package net.insane96mcp.progressivebosses.events.entities;

import java.util.List;

import net.insane96mcp.progressivebosses.lib.Properties;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

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

		int radius = 256;
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
		
		if (!Properties.Dragon.sumKilledDragons && killedCount > 0)
			killedCount /= players.size();
		
		SetHealth(dragon, killedCount);
		
		tags.setInteger("progressivebosses:difficulty", (int) killedCount);
	}
	
	private static void OnDeath(LivingDeathEvent event) {
		
	}
	
	private static void SetHealth(EntityDragon dragon, float killedCount) {
		IAttributeInstance health = dragon.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		health.setBaseValue(health.getBaseValue() + (killedCount * Properties.Dragon.bonusHealthPerKilled));
		dragon.setHealth((float) health.getBaseValue());
	}

	public static void Update(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof EntityDragon))
			return;
		
		World world = event.getEntity().world;
		
		EntityDragon dragon = (EntityDragon)event.getEntity();
		NBTTagCompound tags = dragon.getEntityData();
		
		int difficulty = tags.getInteger("progressivebosses:difficulty");
		
		if (difficulty == 0)
			return;
		
		float health = dragon.getHealth();
		float heal = difficulty / 100f;
		
		if (heal > 0.1f)
			heal = 0.1f;

		if (dragon.ticksExisted % 2 == 0 && dragon.getHealth() < dragon.getMaxHealth() && dragon.getHealth() > 0.0f)
            dragon.setHealth(health + heal);
		
		System.out.println("hp " + dragon.getHealth());
	}
}
