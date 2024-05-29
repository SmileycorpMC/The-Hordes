package net.smileycorp.hordes.config.data.hordeevent;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HordeTableLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ResourceLocation FALLBACK_TABLE = Constants.loc("fallback");
    public static HordeTableLoader INSTANCE = new HordeTableLoader();

    private final Map<ResourceLocation, HordeSpawnTable> SPAWN_TABLES = Maps.newHashMap();

    public HordeTableLoader() {
        super(GSON, "horde_data/tables");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager manager, IProfiler profiller) {
        SPAWN_TABLES.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            try {
                HordeSpawnTable table = HordeSpawnTable.deserialize(entry.getKey(), entry.getValue());
                if (table == null) throw new NullPointerException();
                SPAWN_TABLES.put(entry.getKey(), table);
                HordesLogger.logInfo("loaded horde table " + entry.getKey());
            } catch (Exception e) {
                HordesLogger.logError("Failed to parse table " + entry.getKey(), e);
            }
        }
    }

    public HordeSpawnTable getFallbackTable() {
        return getTable(FALLBACK_TABLE);
    }

    public HordeSpawnTable getTable(ResourceLocation loc) {
        HordeSpawnTable table = SPAWN_TABLES.get(loc);
        if (table == null) HordesLogger.logInfo("Failed loading table " + loc + ", loading fallback table hordes:fallback");
        return table == null ? getFallbackTable() : table;
    }

    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> ctx, SuggestionsBuilder builder) {
        return ISuggestionProvider.suggestResource(INSTANCE.SPAWN_TABLES.keySet(), builder);
    }
    
}
