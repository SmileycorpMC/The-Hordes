package net.smileycorp.hordes.common.hordeevent.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnTable;

import java.util.Map;

public class HordeTableLoader extends SimpleJsonResourceReloadListener {

    public static HordeTableLoader INSTANCE = new HordeTableLoader();
    public static ResourceLocation DEFAULT_TABLE = Constants.loc("default");

    private final Map<ResourceLocation, HordeSpawnTable> SPAWN_TABLES = Maps.newHashMap();

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public HordeTableLoader() {
        super(GSON, "tables");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiller) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            try {
                SPAWN_TABLES.put(entry.getKey(), HordeSpawnTable.deserialize(entry.getKey(), entry.getValue()));
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

}
