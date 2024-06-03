package net.smileycorp.hordes.config.data.hordeevent;

import com.google.common.collect.Sets;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class HordeScriptLoader {

    public static HordeScriptLoader INSTANCE;
    
    private final TreeSet<HordeScript> SCRIPTS = Sets.newTreeSet(HordeScript::sort);
    private final File directory;
    
    public static void init(FMLPreInitializationEvent event) {
        INSTANCE = new HordeScriptLoader(new File(event.getModConfigurationDirectory().getPath() + "/hordes/scripts"));
    }

    public HordeScriptLoader(File directory) {
        this.directory = directory;
    }

    public void loadScripts() {
        JsonParser parser = new JsonParser();
        SCRIPTS.clear();
        for (File file : directory.listFiles((f, s) -> s.endsWith(".json"))) {
            ResourceLocation name =  Constants.loc(file.getName().replace(".json", ""));
            try {
                SCRIPTS.add(HordeScript.deserialize(name, parser.parse(new FileReader(file)).getAsJsonObject()));
            } catch (Exception e) {
                HordesLogger.logError("Failed to parse script " + name, e);
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
