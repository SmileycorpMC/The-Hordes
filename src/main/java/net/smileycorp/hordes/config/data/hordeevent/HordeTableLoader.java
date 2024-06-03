package net.smileycorp.hordes.config.data.hordeevent;

import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

public class HordeTableLoader {

    public static ResourceLocation FALLBACK_TABLE = Constants.loc("fallback");
    public static HordeTableLoader INSTANCE;

    private final Map<ResourceLocation, HordeSpawnTable> SPAWN_TABLES = Maps.newHashMap();
    private final File directory;
    
    public static void init(FMLPreInitializationEvent event) {
        INSTANCE = new HordeTableLoader(new File(event.getModConfigurationDirectory().getPath() + "/hordes/scripts"));
    }
    
    public HordeTableLoader(File directory) {
        this.directory = directory;
    }
    
    public void loadTables() {
        JsonParser parser = new JsonParser();
        SPAWN_TABLES.clear();
        for (File file : directory.listFiles((f, s) -> s.endsWith(".json"))) {
            ResourceLocation name =  Constants.loc(file.getName().replace(".json", ""));
            try {
                HordeSpawnTable table = HordeSpawnTable.deserialize(name, parser.parse(new FileReader(file)).getAsJsonObject());
                if (table == null) throw new NullPointerException();
                SPAWN_TABLES.put(name, table);
                HordesLogger.logInfo("loaded horde table " + name);
            } catch (Exception e) {
                HordesLogger.logError("Failed to parse table " + name, e);
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
    
}
