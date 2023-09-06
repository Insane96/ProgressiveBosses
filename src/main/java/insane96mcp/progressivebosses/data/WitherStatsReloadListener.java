package insane96mcp.progressivebosses.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.data.wither.WitherStats;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class WitherStatsReloadListener extends SimplePreparableReloadListener<Void> {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final WitherStatsReloadListener INSTANCE;

    File jsonConfigFolder = new File(ProgressiveBosses.CONFIG_FOLDER + "/wither");

    public static final Map<Integer, WitherStats> STATS_MAP = new HashMap<>();

    static {
        INSTANCE = new WitherStatsReloadListener();
    }

    @Override
    protected void apply(Void map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        STATS_MAP.clear();
        if (!jsonConfigFolder.exists()) {
            if (!jsonConfigFolder.mkdirs()) {
                LogHelper.warn("Failed to create wither json config folder");
                return;
            }
        }
        FileFilter fileFilter = new WildcardFileFilter("*.json", IOCase.INSENSITIVE);
        File[] fileList = jsonConfigFolder.listFiles(fileFilter);
        if (fileList == null) {
            LogHelper.error("Failed to read %s", jsonConfigFolder.getPath());
            return;
        }
        for (File file : fileList) {
            if (file.isDirectory())
                continue;

            try {
                JsonReader reader = new JsonReader(new FileReader(file.getAbsolutePath()));
                WitherStats witherStats = GSON.fromJson(reader, WitherStats.class);
                STATS_MAP.put(witherStats.level, witherStats);
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
        }

        LogHelper.info("Loaded %s Wither levels", STATS_MAP.size());
    }

    @Override
    protected @NotNull Void prepare(@NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        return null;
    }
}
