package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.capability.DifficultyCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class PlayerClone {

    @SubscribeEvent
    public static void eventPlayerClone(PlayerEvent.Clone event) {
        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getPlayer();

        oldPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(oldDifficulty -> {
            newPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(newDifficulty -> {
               newDifficulty.setSpawnedWithers(oldDifficulty.getSpawnedWithers());
               newDifficulty.setKilledDragons(oldDifficulty.getKilledDragons());
               newDifficulty.setFirstDragon(oldDifficulty.getFirstDragon());
            });
        });
    }
}
