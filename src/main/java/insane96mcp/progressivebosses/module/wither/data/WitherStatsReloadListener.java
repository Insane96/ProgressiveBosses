package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.WitherFeature;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WitherStatsReloadListener extends SimplePreparableReloadListener<Void> {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final WitherStatsReloadListener INSTANCE;

    File file = new File(ProgressiveBosses.CONFIG_FOLDER, "wither.json");

    public static final Map<Integer, WitherStats> STATS_MAP = new HashMap<>();

    static {
        INSTANCE = new WitherStatsReloadListener();
    }

    @Override
    protected void apply(Void map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        STATS_MAP.clear();

        try {
            if (!file.exists()) {
                String json = GSON.toJson(WitherFeature.DEFAULT_WITHER_STATS, WitherStats.LIST_TYPE).replaceAll("  ", "    ");
                Files.write(file.toPath(), json.getBytes());
            }
            JsonReader reader = new JsonReader(new FileReader(file.getAbsolutePath()));
            List<WitherStats> witherStats = GSON.fromJson(reader, WitherStats.LIST_TYPE);
            for (WitherStats witherStats1 : witherStats) {
                STATS_MAP.put(witherStats1.level, witherStats1);
            }
        }
        catch (FileNotFoundException e) {
            LogHelper.error("%s", e.getMessage());
        }
        catch (JsonSyntaxException e) {
            LogHelper.error("Parsing error loading Wither Stat %s: %s", file.getName(), e.getMessage());
        }
        catch (Exception e) {
            LogHelper.error("Failed loading Wither Stat %s: %s", file.getName(), e.getMessage());
        }

        LogHelper.info("Loaded %s Wither levels", STATS_MAP.size());
    }

    @Override
    protected @NotNull Void prepare(@NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        return null;
    }
}
