package net.smileycorp.hordes.config.data;

import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.config.data.hordeevent.HordeScriptLoader;

public class HordesParsingException extends Exception {

    private ResourceLocation script;

    public HordesParsingException(String message) {
        super(message);
        script = HordeScriptLoader.INSTANCE.getCurrentScript();
    }

    public ResourceLocation getScript() {
        return script;
    }

    public void setScript(ResourceLocation script) {
        this.script = script;
    }

}
