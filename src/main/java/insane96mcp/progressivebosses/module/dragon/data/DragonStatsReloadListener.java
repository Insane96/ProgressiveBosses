package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class DragonStatsReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final DragonStatsReloadListener INSTANCE;

    public static final Map<Byte, DragonStats> STATS_MAP = new HashMap<>();

    static {
        INSTANCE = new DragonStatsReloadListener();
    }

    public DragonStatsReloadListener() {
        super(GSON, "progressivebosses/ender_dragon");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        STATS_MAP.clear();
        for (var entry : map.entrySet()) {
            try {
                ResourceLocation name = entry.getKey();
                String[] split = name.getPath().split("/");
                if (split[split.length - 1].startsWith("_"))
                    continue;

                DragonStats dragonStats = GSON.fromJson(entry.getValue(), DragonStats.class);
                if (STATS_MAP.containsKey(dragonStats.level))
                    LogHelper.warn("Duplicate Dragon Stats level: " + dragonStats.level);
                STATS_MAP.put(dragonStats.level, dragonStats);
            }
            catch (JsonSyntaxException e) {
                LogHelper.error("Parsing error loading Dragon Stats %s: %s", entry.getKey(), e.getMessage());
            }
            catch (Exception e) {
                LogHelper.error("Failed loading Dragon Stats %s: %s", entry.getKey(), e.getMessage());
            }
        }

        LogHelper.info("Loaded %s Dragon Stats", STATS_MAP.size());
    }
}
