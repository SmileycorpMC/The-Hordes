package net.smileycorp.hordes.hordeevent.data;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class HordeScriptLoader extends JsonReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static HordeScriptLoader INSTANCE = new HordeScriptLoader();
    
    private final TreeSet<HordeScript> SCRIPTS = Sets.newTreeSet(HordeScript::sort);

    public HordeScriptLoader() {
        super(GSON, "horde_data/scripts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager manager, IProfiler profiller) {
        SCRIPTS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            try {
                SCRIPTS.add(HordeScript.deserialize(entry.getKey(), entry.getValue()));
            } catch (Exception e) {
                HordesLogger.logError("Failed to parse script " + entry.getKey(), e);
            }
        }
        SCRIPTS.forEach(script -> HordesLogger.logInfo("loaded horde script " + script.getName()));
    }

    public Collection<HordeScript> getScripts() {
        return SCRIPTS;
    }

    public Collection<HordeScript> getScripts(HordePlayerEvent event) {
        return getScripts().stream().filter(script -> script.getType() == event.getClass()).collect(Collectors.toList());
    }
    
    public void applyScripts(HordePlayerEvent event) {
        getScripts().stream().filter(script -> script.getType() == event.getClass()
                && script.shouldApply(event.getEntityWorld(), event.getEntity(), event.getPlayer(), event.getRandom())).forEach(script -> {
            script.apply(event);
            HordesLogger.logInfo("Applying script " + script.getName() + " for event " + event);
        });
    }
    
}
