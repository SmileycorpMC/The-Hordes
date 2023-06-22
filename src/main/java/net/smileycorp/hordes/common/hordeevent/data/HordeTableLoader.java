package net.smileycorp.hordes.common.hordeevent.data;

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
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnTable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HordeTableLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ResourceLocation DEFAULT_TABLE = Constants.loc("default");
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
                SPAWN_TABLES.put(entry.getKey(), HordeSpawnTable.deserialize(entry.getKey(), entry.getValue()));
                Hordes.logInfo("loaded horde table " + entry.getKey());
            } catch (Exception e) {
                Hordes.logError("Failed to parse table " + entry.getKey(), e);
            }
        }
    }

    public HordeSpawnTable getDefaultTable() {
        return getTable(DEFAULT_TABLE);
    }

    public HordeSpawnTable getTable(ResourceLocation loc) {
        return SPAWN_TABLES.get(loc);
    }

    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(INSTANCE.SPAWN_TABLES.keySet(), builder);
    }
}
