package net.smileycorp.hordes.config.data.hordeevent;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.smileycorp.atlas.api.data.Sorters;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.data.HordesJsonLoader;
import net.smileycorp.hordes.config.data.HordesParsingException;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class HordeScriptLoader extends HordesJsonLoader {


    public static HordeScriptLoader INSTANCE;

    private final TreeMap<ResourceLocation, HordeScript> SCRIPTS = new TreeMap<>(Sorters::resourceLocation);

    private ResourceLocation current_script = null;
    
    public HordeScriptLoader(FMLPreInitializationEvent event) {
        super(new File(event.getModConfigurationDirectory().getPath() + "/hordes/scripts"));
        INSTANCE = this;
    }

    @Override
    protected boolean shouldLoad() {
        return HordeEventConfig.enableHordeEvent;
    }

    @Override
    protected void dataInit() {
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING HORDE SCRIPTS");
    }

    @Override
    protected void readData(Map<ResourceLocation, JsonElement> map) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            HordesLogger.blankLine();
            try {
                ResourceLocation loc = entry.getKey();
                current_script = loc;
                HordeScript<?> script = HordeScript.deserialize(loc, entry.getValue());
                if (script == null) continue;
                SCRIPTS.put(loc, script);
                HordesLogger.logInfo("loaded horde script " + loc);
            } catch (Exception e) {
                HordesLogger.logError("Failed to parse script " + entry.getKey(), e);
            }
        }
        current_script = null;
    }

    @Override
    public void clearData() {
        SCRIPTS.clear();
    }

    public Collection<HordeScript> getScripts() {
        return SCRIPTS.values();
    }

    public HordeScript getScript(ResourceLocation loc) {
        return SCRIPTS.get(loc);
    }

    public Collection<HordeScript> getScripts(HordePlayerEvent event) {
        return getScripts().stream().filter(script -> script.getType() == event.getClass()).collect(Collectors.toList());
    }

    public <T extends HordePlayerEvent> void applyScripts(T event) {
        HordeContext<T> ctx = new HordeContext<>(event);
        getScripts().stream().filter(script -> script.getType() == event.getClass() && script.shouldApply(ctx)).forEach(script -> {
            script.apply(ctx);
            HordesLogger.logInfo("Applying script " + script.getName() + " for event " + event);
        });
    }

    public ResourceLocation getCurrentScript() {
        return current_script;
    }

    protected Exception error(Exception e, ResourceLocation loc) {
        if (e instanceof JsonParseException) {
            e = new HordesParsingException(e.getMessage());
            ((HordesParsingException) e).setScript(loc);
        }
        return e;
    }
    
}
