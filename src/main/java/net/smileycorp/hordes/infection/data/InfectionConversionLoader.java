package net.smileycorp.hordes.infection.data;

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
import net.minecraft.world.entity.Mob;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.infection.InfectedEffect;

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
        HordesLogger.logInfo("Loading conversion tables");
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = new ResourceLocation(id, "infection_conversions");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.logInfo("Loading conversion table " + loc);
                for (JsonElement element : json.getAsJsonArray()) {
                    try {
                       InfectionConversionEntry entry = InfectionConversionEntry.deserialize(element.getAsJsonObject());
                       conversionTable.put(entry.getEntity(), entry);
                    } catch (Exception e) {
                       HordesLogger.logError("Failed to load conversion entry " + element.getAsString(), e);
                    }
                }
            } catch (Exception e) {
                HordesLogger.logError("Failed to load conversion table " + loc, e);
            }
        }
    }

    public void tryToInfect(LivingEntity entity) {
        InfectionConversionEntry entry = conversionTable.get(entity.getType());
        if (entry != null && entry.shouldInfect(entity)) InfectedEffect.apply(entity);
    }

    public boolean canBeInfected(Entity entity) {
        if (!(entity instanceof Mob)) return false;
        return conversionTable.containsKey(entity.getType());
    }

    public boolean convertEntity(Mob entity) {
        InfectionConversionEntry entry = conversionTable.get(entity.getType());
        if (entry != null) return entry.convertEntity(entity) != null;
        return false;
    }

}
