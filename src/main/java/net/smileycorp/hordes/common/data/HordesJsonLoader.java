package net.smileycorp.hordes.common.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.smileycorp.hordes.common.HordesLogger;
import org.apache.commons.compress.utils.Lists;

import java.io.Reader;
import java.util.List;
import java.util.Map;

public abstract class HordesJsonLoader extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final String directory;
    protected final List<Pair<String, Exception>> deferredExceptions = Lists.newArrayList();

    public HordesJsonLoader(String directory) {
        this.directory = directory;
    }
    
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager rm, ProfilerFiller profiler) {
        deferredExceptions.clear();
        Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
        FileToIdConverter filetoidconverter = FileToIdConverter.json(directory);

        for(Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(rm).entrySet()) {
            ResourceLocation file = entry.getKey();
            ResourceLocation id = filetoidconverter.fileToId(file);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                if (map.put(id, json) != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                }
            } catch (Exception e) {
                deferredExceptions.add(new Pair<>("Couldn't parse data " + file, error(e, id)));
            }
        }
        return map;
    }

    protected Exception error(Exception e, ResourceLocation loc) {
        if (e instanceof JsonParseException) e = new HordesParsingException(e.getMessage());
        return e;
    }

    protected void printDeferredExceptions() {
        for (Pair<String, Exception> pair : deferredExceptions) {
            HordesLogger.blankLine();
            HordesLogger.logError(pair.getFirst(), pair.getSecond());
        }
        deferredExceptions.clear();
    }
    
}
