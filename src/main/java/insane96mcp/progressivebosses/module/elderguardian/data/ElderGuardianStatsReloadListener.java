package insane96mcp.progressivebosses.module.elderguardian.data;

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

public class ElderGuardianStatsReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final ElderGuardianStatsReloadListener INSTANCE;

    public static final Map<Integer, ElderGuardianStats> STATS_MAP = new HashMap<>();

    static {
        INSTANCE = new ElderGuardianStatsReloadListener();
    }

    public ElderGuardianStatsReloadListener() {
        super(GSON, "progressivebosses/elder_guardian");
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

                ElderGuardianStats elderGuardianStats = GSON.fromJson(entry.getValue(), ElderGuardianStats.class);
                if (elderGuardianStats.level > 2)
                    throw new IndexOutOfBoundsException("Level out of bounds, must be between 0 and 2: " + elderGuardianStats.level);
                if (STATS_MAP.containsKey(elderGuardianStats.level))
                    throw new IllegalArgumentException("Duplicate Elder Guardian Stats level: " + elderGuardianStats.level);
                STATS_MAP.put(elderGuardianStats.level, elderGuardianStats);
            }
            catch (JsonSyntaxException e) {
                LogHelper.error("Parsing error loading Elder Guardian Stats %s: %s", entry.getKey(), e.getMessage());
            }
            catch (Exception e) {
                LogHelper.error("Failed loading Elder Guardian Stats %s: %s", entry.getKey(), e.getMessage());
            }
        }

        LogHelper.info("Loaded %s Elder Guardian Stats", STATS_MAP.size());
    }
}
