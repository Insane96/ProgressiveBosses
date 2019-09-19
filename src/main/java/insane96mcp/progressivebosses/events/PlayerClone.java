package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class PlayerClone {
    @SubscribeEvent
    public static void eventPlayerClone(PlayerEvent.Clone event) {
        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getPlayer();

        CompoundNBT oldPlayerData = oldPlayer.getPersistentData();
        newPlayer.getPersistentData().putInt("progressivebosses:spawnedwithers", oldPlayerData.getInt("progressivebosses:spawnedwithers"));
        newPlayer.getPersistentData().putInt("progressivebosses:killeddragons", oldPlayerData.getInt("progressivebosses:killeddragons"));
    }
}
