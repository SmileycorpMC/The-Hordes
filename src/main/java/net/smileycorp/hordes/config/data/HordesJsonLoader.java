package net.smileycorp.hordes.config.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

public abstract class HordesJsonLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final File directory;

    public HordesJsonLoader(File directory) {
        this.directory = directory;
    }
    
    public final void loadData() {
        if (!shouldLoad()) return;
        dataInit();
        Map<ResourceLocation, JsonElement> data = Maps.newHashMap();
        File[] files = directory.listFiles((f, s) -> s != null && s.endsWith(".json"));
        if (files == null) {
            HordesLogger.logError("Failed reading files in " + directory, new NullPointerException());
            return;
        }
        for (File file : files) {
            ResourceLocation id = Constants.loc(file.getName().replace(".json", ""));
            try {
                JsonReader reader = new JsonReader(new FileReader(file));
                JsonElement json = GSON.getAdapter(JsonElement.class).read(reader);
                if (data.put(id, json) != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                }
            } catch (Exception e) {
                HordesLogger.blankLine();
                HordesLogger.logError("Couldn't parse data " + file, error(e, id));
            }
        }
        readData(data);
    }

    protected abstract boolean shouldLoad();

    protected abstract void dataInit();

    protected abstract void readData(Map<ResourceLocation, JsonElement> data);

    public abstract void clearData();

    protected Exception error(Exception e, ResourceLocation loc) {
        if (e instanceof JsonParseException) e = new HordesParsingException(e.getMessage());
        return e;
    }

}
