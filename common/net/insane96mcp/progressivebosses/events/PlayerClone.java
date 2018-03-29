package net.insane96mcp.progressivebosses.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerClone {
	@SubscribeEvent
	public static void EventPlayerClone(PlayerEvent.Clone event) {
		EntityPlayer oldPlayer = event.getOriginal();
		EntityPlayer newPlayer = event.getEntityPlayer();
		
		NBTTagCompound oldPlayerData = oldPlayer.getEntityData();
		newPlayer.getEntityData().merge(oldPlayerData);
	}
}
