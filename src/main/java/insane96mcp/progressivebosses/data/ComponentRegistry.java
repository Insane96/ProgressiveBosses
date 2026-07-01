package insane96mcp.progressivebosses.data;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.data.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ComponentRegistry {
    private static final Map<ResourceLocation, Type> COMPONENTS = new HashMap<>();

    public static void init() {
        registerComponent(ProgressiveBosses.id("sitting_attack"), SittingAttackComponent.class);
        registerComponent(ProgressiveBosses.id("health"), HealthComponent.class);
        registerComponent(ProgressiveBosses.id("vulnerabilities"), VulnerabilitiesComponent.class);
        registerComponent(ProgressiveBosses.id("fly_speed"), FlySpeedComponent.class);
        registerComponent(ProgressiveBosses.id("anger"), AngerComponent.class);
        registerComponent(ProgressiveBosses.id("melee_damage"), MeleeDamageComponent.class);
        registerComponent(ProgressiveBosses.id("blast_attack"), BlastAttackComponent.class);
        registerComponent(ProgressiveBosses.id("strafe_player"), StrafePlayerComponent.class);
        registerComponent(ProgressiveBosses.id("acidball"), AcidballComponent.class);
        registerComponent(ProgressiveBosses.id("charge_player"), ChargePlayerComponent.class);
        registerComponent(ProgressiveBosses.id("land"), LandComponent.class);
        registerComponent(ProgressiveBosses.id("spikes"), SpikesComponent.class);
        registerComponent(ProgressiveBosses.id("crystal_respawn"), CrystalRespawnComponent.class);
        registerComponent(ProgressiveBosses.id("minion"), MinionComponent.class);
        registerComponent(ProgressiveBosses.id("loot"), LootComponent.class);
    }

    public static void registerComponent(ResourceLocation id, Type type) {
        COMPONENTS.put(id, type);
    }

    @Nullable
    public static Type getComponent(ResourceLocation id) {
        return COMPONENTS.get(id);
    }
}
