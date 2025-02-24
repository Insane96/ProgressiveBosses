package insane96mcp.progressivebosses.module.dragon.data;

import insane96mcp.progressivebosses.data.BossComponent;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface PhaseChanger {
    int getPriority();
    EnderDragonPhase<?> getPhase();
    boolean shouldExecute(EnderDragon dragon);
    void execute(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin);
    default void onPhaseBegin(DragonPhaseEvent.Begin event, EnderDragon dragon) {};

    @Nullable
    static PhaseChanger getPhaseChanger(EnderDragon dragon) {
        DragonDefinition definition = DragonFeature.getDragonDefinition(dragon).orElse(null);
        if (definition == null)
            return null;
        List<PhaseChanger> phases = new ArrayList<>();
        for (BossComponent component : definition.components) {
            if (component instanceof PhaseChanger phaseChanger
                    && phaseChanger.shouldExecute(dragon))
                phases.add(phaseChanger);
        }

        if (phases.isEmpty())
            return null;

        int maxPriority = phases.stream().mapToInt(PhaseChanger::getPriority).max().getAsInt();
        phases = phases.stream()
                .filter(phase -> phase.getPriority() == maxPriority)
                .collect(Collectors.toList());

        return phases.get(dragon.getRandom().nextInt(phases.size()));
    }
}
