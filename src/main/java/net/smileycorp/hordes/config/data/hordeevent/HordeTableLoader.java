package net.smileycorp.hordes.config.data.hordeevent;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.data.HordesJsonLoader;
import net.smileycorp.hordes.hordeevent.HordeSpawnEntry;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;

import java.io.File;
import java.util.Map;

public class HordeTableLoader extends HordesJsonLoader {

    public static ResourceLocation FALLBACK_TABLE = Constants.loc("fallback");
    public static HordeTableLoader INSTANCE;

    private final Map<ResourceLocation, HordeSpawnTable> SPAWN_TABLES = Maps.newHashMap();

    public HordeTableLoader(FMLPreInitializationEvent event) {
        super(new File(event.getModConfigurationDirectory().getPath() + "/hordes/tables"));
        INSTANCE = this;
    }

    @Override
    protected boolean shouldLoad() {
        return HordeEventConfig.enableHordeEvent;
    }

    @Override
    protected void dataInit() {
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING HORDE TABLES");
        try {
            SPAWN_TABLES.put(FALLBACK_TABLE, new HordeSpawnTable.Builder("fallback").addEntry(
                    new HordeSpawnEntry(ForgeRegistries.ENTITIES.getValue(new ResourceLocation("zombie")))
                            .setNBT(JsonToNBT.getTagFromJson("{ArmorItems:[{},{},{},{id:pumpkin,Count:1}]}"))).build());
        } catch (Exception e) {
            HordesLogger.logError("Failed registering fallback table", e);
        }
    }

    @Override
    protected void readData(Map<ResourceLocation, JsonElement> data) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : data.entrySet()) {
            try {
                HordesLogger.blankLine();
                HordeSpawnTable table = HordeSpawnTable.deserialize(entry.getKey(), entry.getValue());
                if (table == null) throw new NullPointerException();
                SPAWN_TABLES.put(entry.getKey(), table);
                HordesLogger.logInfo("loaded horde table " + entry.getKey());
            } catch (Exception e) {
                HordesLogger.logError("Failed to parse table " + entry.getKey(), e);
            }
        }
    }

    @Override
    public void clearData() {
        SPAWN_TABLES.clear();
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
