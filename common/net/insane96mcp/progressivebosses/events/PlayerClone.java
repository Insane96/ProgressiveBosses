package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class PlayerClone {
	@SubscribeEvent
	public static void EventPlayerClone(PlayerEvent.Clone event) {
		EntityPlayer oldPlayer = event.getOriginal();
		EntityPlayer newPlayer = event.getEntityPlayer();
		
		NBTTagCompound oldPlayerData = oldPlayer.getEntityData();

		//Fix Potion Core Death Loop
		oldPlayerData.removeTag("Potion Core - Health Fix");
		
		newPlayer.getEntityData().merge(oldPlayerData);
	}
}
