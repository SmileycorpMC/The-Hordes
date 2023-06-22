package net.smileycorp.hordes.common.infection.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.infection.InfectedEffect;
import net.smileycorp.hordes.common.infection.InfectionConversionEntry;

import java.util.HashMap;
import java.util.Map;

public class InfectionConversionLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static InfectionConversionLoader INSTANCE = new InfectionConversionLoader();

    private final Map<EntityType<?>, InfectionConversionEntry> conversionTable = new HashMap<>();

    public InfectionConversionLoader() {
        super(GSON, "horde_data");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiller) {
        conversionTable.clear();
        for (ResourceLocation a : map.keySet()) Hordes.logInfo(a);
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = new ResourceLocation(id, "infection_conversions");
            JsonElement json = map.get(loc);
            if (json == null) return;
            try {
                for (JsonElement element : json.getAsJsonArray()) {
                    try {
                       InfectionConversionEntry entry = InfectionConversionEntry.deserialize(element.getAsJsonObject());
                       conversionTable.put(entry.getEntity(), entry);
                    } catch (Exception e) {
                       Hordes.logError("Failed to load conversion entry " + element.getAsString(), e);
                    }
                }
            } catch (Exception e) {
                Hordes.logError("Failed to load conversion table " + loc, e);
            }
        }
    }

    public void tryToInfect(LivingEntity entity) {
        InfectionConversionEntry entry = conversionTable.get(entity.getType());
        if (entry != null && entry.shouldInfect(entity)) InfectedEffect.apply(entity);
    }

    public boolean canBeInfected(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return conversionTable.containsKey(entity.getType());
    }

    public void convertEntity(LivingEntity entity) {
        InfectionConversionEntry entry = conversionTable.get(entity.getType());
        if (entry != null) entry.convertEntity(entity);
    }

}
