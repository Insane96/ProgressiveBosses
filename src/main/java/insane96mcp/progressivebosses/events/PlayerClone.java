package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.capability.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class PlayerClone {

    @SubscribeEvent
    public static void eventPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getPlayer();

        oldPlayer.getCapability(Difficulty.INSTANCE).ifPresent(oldDifficulty -> {
            newPlayer.getCapability(Difficulty.INSTANCE).ifPresent(newDifficulty -> {
               newDifficulty.setSpawnedWithers(oldDifficulty.getSpawnedWithers());
               newDifficulty.setKilledDragons(oldDifficulty.getKilledDragons());
               newDifficulty.setFirstDragon(oldDifficulty.getFirstDragon());
            });
        });
    }
}
