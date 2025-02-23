package insane96mcp.progressivebosses.data;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.data.DragonHealthComponent;
import insane96mcp.progressivebosses.module.dragon.data.DragonVulnerabilitiesComponent;
import insane96mcp.progressivebosses.module.dragon.data.SittingAttackComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ComponentRegistry {
    private static final Map<ResourceLocation, Type> COMPONENTS = new HashMap<>();

    public static void init() {
        registerComponent(new ResourceLocation(ProgressiveBosses.MOD_ID, "sitting_attack"), SittingAttackComponent.class);
        registerComponent(new ResourceLocation(ProgressiveBosses.MOD_ID, "health"), DragonHealthComponent.class);
        registerComponent(new ResourceLocation(ProgressiveBosses.MOD_ID, "vulnerabilities"), DragonVulnerabilitiesComponent.class);
    }

    public static void registerComponent(ResourceLocation id, Type type) {
        COMPONENTS.put(id, type);
    }

    @Nullable
    public static Type getComponent(ResourceLocation id) {
        return COMPONENTS.get(id);
    }
}
