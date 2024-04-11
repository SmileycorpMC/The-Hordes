package net.smileycorp.hordes.infection.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.InfectEntityEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectedEffect;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;
import net.smileycorp.hordes.infection.network.SyncImmunityItemsMessage;
import net.smileycorp.hordes.infection.network.SyncWearableProtectionMessage;

import java.util.Map;

public class InfectionDataLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static InfectionDataLoader INSTANCE = new InfectionDataLoader();

    private final Map<EntityType<?>, InfectionConversionEntry> conversionTable = Maps.newHashMap();
    private final Map<Item, Integer> immunityItems = Maps.newHashMap();
    
    private final Map<Item, Float> wearablesProtection = Maps.newHashMap();

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
                        Item item = ForgeRegistries.ITEMS.getValue(name);
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
            ResourceLocation loc = new ResourceLocation(id, "immune_wearables");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.logInfo("Loading wearables protection list " + loc);
                for (JsonElement element : json.getAsJsonArray()) {
                    try {
                        JsonObject obj = element.getAsJsonObject();
                        ResourceLocation name = new ResourceLocation(obj.get("item").getAsString());
                        Item item = ForgeRegistries.ITEMS.getValue(name);
                        float modifier = obj.get("multiplier").getAsFloat();
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
    }

    public void tryToInfect(LivingEntity entity, LivingEntity attacker, DamageSource source, float amount) {
        if (MinecraftForge.EVENT_BUS.post(new InfectEntityEvent(entity, attacker, source, amount))) return;
        if ((entity instanceof Player && InfectionConfig.infectPlayers.get()))
            if (entity.getRandom().nextFloat() <= getModifiedInfectChance(entity, (float)(double)InfectionConfig.playerInfectChance.get()))
                InfectedEffect.apply(entity);
        if ((entity instanceof Villager && InfectionConfig.infectVillagers.get()))
            if (entity.getRandom().nextFloat() <= getModifiedInfectChance(entity, (float)(double)InfectionConfig.villagerInfectChance.get()))
                InfectedEffect.apply(entity);
        InfectionConversionEntry entry = conversionTable.get(entity.getType());
        if (entry != null && entry.shouldInfect(entity)) InfectedEffect.apply(entity);
    }

    public boolean canBeInfected(Entity entity) {
        if (entity instanceof Player) return InfectionConfig.infectPlayers.get();
        if (entity instanceof Villager && InfectionConfig.infectVillagers.get()) return true;
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
    
    public float getModifiedInfectChance(LivingEntity entity, float chance) {
        for (EquipmentSlot slot : EquipmentSlot.values()) if (slot.isArmor())
            chance *= getProtectionMultiplier(entity.getItemBySlot(slot));
        return chance;
    }
    
    public float getProtectionMultiplier(ItemStack stack) {
        if (stack.isEmpty()) return 1;
        Item item = stack.getItem();
        return wearablesProtection.containsKey(item) ? wearablesProtection.get(item) : 1;
    }
    
    public void syncData(Connection connection) {
        InfectionPacketHandler.sendTo(new SyncImmunityItemsMessage(immunityItems), connection, NetworkDirection.PLAY_TO_CLIENT);
        InfectionPacketHandler.sendTo(new SyncWearableProtectionMessage(wearablesProtection), connection, NetworkDirection.PLAY_TO_CLIENT);
    }

}
