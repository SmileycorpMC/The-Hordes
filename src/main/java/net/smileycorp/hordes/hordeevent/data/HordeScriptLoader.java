package net.smileycorp.hordes.hordeevent.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeScript;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HordeScriptLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static HordeScriptLoader INSTANCE = new HordeScriptLoader();

    private final Map<ResourceLocation, HordeScript> SCRIPTS = Maps.newHashMap();

    public HordeScriptLoader() {
        super(GSON, "horde_data/scripts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiller) {
        SCRIPTS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            try {
                SCRIPTS.put(entry.getKey(), HordeScript.deserialize(entry.getKey(), entry.getValue()));
                Hordes.logInfo("loaded horde script " + entry.getKey());
            } catch (Exception e) {
                Hordes.logError("Failed to parse script " + entry.getKey(), e);
            }
        }
    }

    public Collection<HordeScript> getScripts() {
        return SCRIPTS.values();
    }

    public Collection<HordeScript> getScripts(HordePlayerEvent event) {
        List<HordeScript> list = Lists.newArrayList();
        for (HordeScript script : getScripts()) {
            if (script.getType() == event.getClass()) list.add(script);
        }
        return list;
    }

}
