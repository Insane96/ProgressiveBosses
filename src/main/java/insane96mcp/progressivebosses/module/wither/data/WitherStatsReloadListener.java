package insane96mcp.progressivebosses.module.wither.data;

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

public class WitherStatsReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final WitherStatsReloadListener INSTANCE;

    public static final Map<Integer, WitherStats> STATS_MAP = new HashMap<>();

    static {
        INSTANCE = new WitherStatsReloadListener();
    }

    public WitherStatsReloadListener() {
        super(GSON, "progressivebosses/wither");
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

                if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().entrySet().isEmpty())
                    continue;
                WitherStats witherStats = GSON.fromJson(entry.getValue(), WitherStats.class);
                if (STATS_MAP.containsKey(witherStats.level))
                    LogHelper.warn("Duplicate Wither Stats level: " + witherStats.level);
                STATS_MAP.put(witherStats.level, witherStats);
            }
            catch (JsonSyntaxException e) {
                LogHelper.error("Parsing error loading Wither Stats %s: %s", entry.getKey(), e.getMessage());
            }
            catch (Exception e) {
                LogHelper.error("Failed loading Wither Stats %s: %s", entry.getKey(), e.getMessage());
            }
        }

        LogHelper.info("Loaded %s Wither Stats", STATS_MAP.size());
    }
}
