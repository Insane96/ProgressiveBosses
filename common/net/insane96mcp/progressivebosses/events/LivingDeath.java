package net.insane96mcp.progressivebosses.events;

import java.util.List;

import net.insane96mcp.progressivebosses.lib.Properties;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingDeath {

	@SubscribeEvent
	public static void EventLivingDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof EntityDragon))
			return;
		
		EntityDragon dragon = (EntityDragon)event.getEntity();

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
}
