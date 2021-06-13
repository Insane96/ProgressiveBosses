package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.base.Strings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class PlayerClone {

    //This might not be needed, but better safe than sorry
    @SubscribeEvent
    public static void eventPlayerClone(PlayerEvent.Clone event) {
        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getPlayer();

        CompoundNBT oldPlayerData = oldPlayer.getPersistentData();
        newPlayer.getPersistentData().putInt(Strings.Tags.SPAWNED_WITHERS, oldPlayerData.getInt(Strings.Tags.SPAWNED_WITHERS));
        newPlayer.getPersistentData().putInt(Strings.Tags.KILLED_DRAGONS, oldPlayerData.getInt(Strings.Tags.KILLED_DRAGONS));
        newPlayer.getPersistentData().putInt(Strings.Tags.FIRST_DRAGON, oldPlayerData.getInt(Strings.Tags.FIRST_DRAGON));
    }
}
