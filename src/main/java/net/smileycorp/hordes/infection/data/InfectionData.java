package net.smileycorp.hordes.infection.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.InfectEntityEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectedEffect;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;
import net.smileycorp.hordes.infection.network.SyncImmunityItemsMessage;
import net.smileycorp.hordes.infection.network.SyncWearableProtectionMessage;

import java.util.List;
import java.util.Map;

public class InfectionData extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static InfectionData INSTANCE = new InfectionData();

    private final Map<EntityType<?>, InfectionConversionEntry> conversionTable = Maps.newHashMap();
    private final Map<Item, Integer> immunityItems = Maps.newHashMap();
    private final Map<Item, Float> wearablesProtection = Maps.newHashMap();
    private final Map<EntityType<?>, Float> entityInfectChance = Maps.newHashMap();

    public InfectionData() {
        super(GSON, "infection");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiller) {
        conversionTable.clear();
        HordesLogger.logInfo("Loading conversion tables");
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = ResourceLocation.tryBuild(id, "infection_conversions");
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
            ResourceLocation loc = ResourceLocation.tryBuild(id, "immunity_items");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.logInfo("Loading immunity item list " + loc);
                for (JsonElement element : json.getAsJsonArray()) {
                    try {
                        JsonObject obj = element.getAsJsonObject();
                        ResourceLocation name = ResourceLocation.tryParse(obj.get("item").getAsString());
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
        wearablesProtection.clear();
        HordesLogger.logInfo("Loading wearables protection list");
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = ResourceLocation.tryBuild(id, "wearables_protection");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.logInfo("Loading wearables protection list " + loc);
                for (JsonElement element : json.getAsJsonArray()) {
                    try {
                        JsonObject obj = element.getAsJsonObject();
                        ResourceLocation name = ResourceLocation.tryParse(obj.get("item").getAsString());
                        Item item = BuiltInRegistries.ITEM.get(name);
                        float modifier = obj.get("protection").getAsFloat();
                        if (item == null || item == Items.AIR) throw new NullPointerException();
                        wearablesProtection.put(item, modifier);
                        HordesLogger.logInfo("Loaded wearable protection " + name + " with modifier " + modifier);
                    } catch (Exception e) {
                        HordesLogger.logError("Failed to load wearable protection " + element.getAsString(), e);
                    }
                }
            } catch (Exception e) {
                HordesLogger.logError("Failed to load wearable protection list " + loc, e);
            }
        }
        entityInfectChance.clear();
        HordesLogger.logInfo("Loading entity infection chances.");
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = ResourceLocation.tryBuild(id, "infection_entities");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.logInfo("Loading entity infection list " + loc);
                for (JsonElement element : json.getAsJsonArray()) {
                    try {
                        JsonObject obj = element.getAsJsonObject();
                        ResourceLocation name = ResourceLocation.tryParse(obj.get("entity").getAsString());
                        EntityType entity = BuiltInRegistries.ENTITY_TYPE.get(name);
                        float chance = obj.get("chance").getAsFloat();
                        entityInfectChance.put(entity, chance);
                        HordesLogger.logInfo("Loaded infection entity " + name + " with infect chance " + chance);
                    } catch (Exception e) {
                        HordesLogger.logError("Failed to infection entity " + element.getAsString(), e);
                    }
                }
            } catch (Exception e) {
                HordesLogger.logError("Failed to load entity infection list " + loc, e);
            }
        }
    }

    public void tryToInfect(LivingEntity entity, LivingEntity attacker, DamageSource source, float amount) {
        if (NeoForge.EVENT_BUS.post(new InfectEntityEvent(entity, attacker, source, amount)).isCanceled()) return;
        if ((entity instanceof Player && InfectionConfig.infectPlayers.get()))
            if (entity.getRandom().nextFloat() <= getInfectionChance(entity, attacker))
                InfectedEffect.apply(entity);
        InfectionConversionEntry entry = conversionTable.get(entity.getType());
        if (entry != null && entry.shouldInfect(entity, attacker)) InfectedEffect.apply(entity);
    }

    public boolean canBeInfected(Entity entity) {
        if (entity instanceof Player) return InfectionConfig.infectPlayers.get();
        if (!(entity instanceof Mob)) return false;
        return conversionTable.containsKey(entity.getType());
    }

    public boolean convertEntity(Mob entity) {
        InfectionConversionEntry entry = conversionTable.get(entity.getType());
        if (entry != null) return entry.convertEntity(entity) != null;
        return false;
    }
    
    public int getImmunityLength(ItemStack stack) {
        return immunityItems.containsKey(stack.getItem()) ? immunityItems.get(stack.getItem()) : 0;
    }
    
    public boolean applyImmunity(LivingEntity entity, Item item) {
        if (immunityItems.containsKey(item)) {
            entity.addEffect(new MobEffectInstance(HordesInfection.IMMUNITY, immunityItems.get(item) * 20));
            return true;
        }
        return false;
    }
    
    public float getInfectionChance(LivingEntity entity, LivingEntity attacker) {
        float base = entityInfectChance.get(attacker.getType());
        float protection = (float) entity.getAttributeValue(HordesInfection.INFECTION_RESISTANCE);
        float total = base * (1 - protection);
        return total;
    }
    
    public float getProtection(EntityType<?> type) {
        return type == EntityType.PLAYER ? (float) (double) InfectionConfig.playerInfectionResistance.get() :
                conversionTable.containsKey(type) ? conversionTable.get(type).protection : 0;
    }
    
    public float getProtectionMultiplier(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        return wearablesProtection.containsKey(item) ? wearablesProtection.get(item) : 0;
    }
    
    public boolean canCauseInfection(EntityType<?> entity) {
        return entityInfectChance.containsKey(entity);
    }
    
    public boolean canCauseInfection(Entity entity) {
        return entity instanceof LivingEntity && entityInfectChance.containsKey(entity.getType());
    }
    
    public void syncData(ServerPlayer player) {
        InfectionPacketHandler.sendTo(new SyncImmunityItemsMessage(immunityItems), player);
        InfectionPacketHandler.sendTo(new SyncWearableProtectionMessage(wearablesProtection), player);
    }
    
    public void readImmunityItems(List<Map.Entry<Item, Integer>> data) {
        immunityItems.clear();
        data.forEach(e -> immunityItems.put(e.getKey(), e.getValue()));
    }
    
    public void readWearableProtection(List<Pair<Item, Float>> data) {
        wearablesProtection.clear();
        data.forEach(e -> wearablesProtection.put(e.getFirst(), e.getSecond()));
    }
    
    public void clear() {
        immunityItems.clear();
        wearablesProtection.clear();
    }
    
}
