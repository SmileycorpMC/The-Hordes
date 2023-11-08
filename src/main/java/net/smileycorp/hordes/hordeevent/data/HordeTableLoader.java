package net.smileycorp.hordes.hordeevent.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HordeTableLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ResourceLocation FALLBACK_TABLE = Constants.loc("fallback");
    public static HordeTableLoader INSTANCE = new HordeTableLoader();

    private final Map<ResourceLocation, HordeSpawnTable> SPAWN_TABLES = Maps.newHashMap();

    public HordeTableLoader() {
        super(GSON, "horde_data/tables");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiller) {
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

    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(INSTANCE.SPAWN_TABLES.keySet(), builder);
    }
}
