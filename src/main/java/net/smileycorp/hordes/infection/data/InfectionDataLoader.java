package net.smileycorp.hordes.infection.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.NeoForge;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.InfectEntityEvent;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectedEffect;

import java.util.HashMap;
import java.util.Map;

public class InfectionDataLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static InfectionDataLoader INSTANCE = new InfectionDataLoader();

    private final Map<EntityType<?>, InfectionConversionEntry> conversionTable = new HashMap<>();
    private final Map<Item, Integer> immunityItems = new HashMap<>();

    public InfectionDataLoader() {
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
        immunityItems.clear();
        HordesLogger.logInfo("Loading immunity item list");
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = new ResourceLocation(id, "immunity_items");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.logInfo("Loading immunity item list " + loc);
                for (JsonElement element : json.getAsJsonArray()) {
                    try {
                        JsonObject obj = element.getAsJsonObject();
                        ResourceLocation name = new ResourceLocation(obj.get("item").getAsString());
                        Item item = BuiltInRegistries.ITEM.get(name);
                        int duration = obj.get("duration").getAsInt();
                        immunityItems.put(item, duration);
                        HordesLogger.logInfo("Loaded immunity item " + name + " with duration " + duration);
                    } catch (Exception e) {
                        HordesLogger.logError("Failed to load immunity item " + element.getAsString(), e);
                    }
                }
            } catch (Exception e) {
                HordesLogger.logError("Failed to load immunity item list " + loc, e);
            }
        }
    }

    public void tryToInfect(LivingEntity entity, LivingEntity attacker, DamageSource source, float amount) {
        if (NeoForge.EVENT_BUS.post(new InfectEntityEvent(entity, attacker, source, amount)).isCanceled()) return;
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

    public boolean applyImmunity(LivingEntity entity, Item item) {
        if (immunityItems.containsKey(item)) {
            entity.addEffect(new MobEffectInstance(HordesInfection.IMMUNITY.get(), immunityItems.get(item) * 20));
            return true;
        }
        return false;
    }

}
