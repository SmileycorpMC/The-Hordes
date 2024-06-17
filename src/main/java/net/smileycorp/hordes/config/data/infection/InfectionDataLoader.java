package net.smileycorp.hordes.config.data.infection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.InfectEntityEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.PotionInfected;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;
import net.smileycorp.hordes.infection.network.SyncCuresMessage;
import net.smileycorp.hordes.infection.network.SyncImmunityItemsMessage;
import net.smileycorp.hordes.infection.network.SyncWearableProtectionMessage;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class InfectionDataLoader {

    public static InfectionDataLoader INSTANCE;
    
    private final List<ItemStack> cures = Lists.newArrayList();
    private final List<Class<? extends EntityLivingBase>> infectionEntities =  Lists.newArrayList();
    private final Map<Class<? extends EntityLivingBase>, InfectionConversionEntry> conversionTable = Maps.newHashMap();
    private final Map<ItemStack, Integer> immunityItems = Maps.newHashMap();
    
    private final Map<Item, Float> wearablesProtection = Maps.newHashMap();
    
    private final Path directory;
    
    public static void init(FMLPreInitializationEvent event) {
        INSTANCE = new InfectionDataLoader(new File(event.getModConfigurationDirectory().getPath() + "/hordes/"));
    }
    
    public InfectionDataLoader(File directory) {
        this.directory = directory.toPath();
    }
    
    public void loadInfectionData() {
        JsonParser parser = new JsonParser();
        HordesLogger.logInfo("Loading infection cures");
        cures.clear();
        try {
            for (JsonElement element : parser.parse(new FileReader(directory.resolve("infection_cures.json").toFile())).getAsJsonArray()) {
                try {
                   ItemStack stack = parseStack(element);
                   if (stack != null) {
                       cures.add(stack);
                       HordesLogger.logInfo("Loaded infection cure " + stack);
                   }
                } catch (Exception e) {
                    HordesLogger.logError("Failed to load cure entry " + element.getAsString(), e);
                }
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load infection cures", e);
        }
        HordesLogger.logInfo("Loading infection entities");
        infectionEntities.clear();
        try {
            for (JsonElement element : parser.parse(new FileReader(directory.resolve("infection_entities.json").toFile())).getAsJsonArray()) {
                try {
                    ResourceLocation loc = new ResourceLocation(element.getAsString());
                    if (ForgeRegistries.ENTITIES.containsKey(loc)) {
                        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(loc);
                        Class<?> clazz = entry.getEntityClass();
                        if (EntityLivingBase.class.isAssignableFrom(clazz)) {
                            infectionEntities.add((Class<? extends EntityLivingBase>) clazz);
                            HordesLogger.logInfo("Loaded infection entity " + loc + " as " + clazz.toString());
                        }
                    }
                } catch (Exception e) {
                    HordesLogger.logError("Failed to load infection entry " + element.getAsString(), e);
                }
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load infection entities", e);
        }
        conversionTable.clear();
        HordesLogger.logInfo("Loading conversion tables");
        try {
            for (JsonElement element : parser.parse(new FileReader(directory.resolve("infection_conversions.json").toFile())).getAsJsonArray()) {
                try {
                   InfectionConversionEntry entry = InfectionConversionEntry.deserialize(element.getAsJsonObject());
                   conversionTable.put((Class<? extends EntityLivingBase>) entry.getEntity().getEntityClass(), entry);
                } catch (Exception e) {
                   HordesLogger.logError("Failed to load conversion entry " + element.toString(), e);
                }
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load conversion table", e);
        }
        immunityItems.clear();
        HordesLogger.logInfo("Loading immunity item list");
        try {
            for (JsonElement element : parser.parse(new FileReader(directory.resolve("immunity_items.json").toFile())).getAsJsonArray()) {
                try {
                    JsonObject obj = element.getAsJsonObject();
                    ItemStack stack = parseStack(obj.get("item"));
                    int duration = obj.get("duration").getAsInt();
                    if (stack == null) throw new NullPointerException();
                    immunityItems.put(stack, duration);
                    HordesLogger.logInfo("Loaded immunity item " + stack + " with duration " + duration);
                } catch (Exception e) {
                    HordesLogger.logError("Failed to load immunity item " + element.toString(), e);
                }
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load immunity item list", e);
        }
        wearablesProtection.clear();
        HordesLogger.logInfo("Loading wearables protection list");
        try {
            for (JsonElement element : parser.parse(new FileReader(directory.resolve("immune_wearables.json").toFile())).getAsJsonArray()) {
                try {
                    JsonObject obj = element.getAsJsonObject();
                    ResourceLocation name = new ResourceLocation(obj.get("item").getAsString());
                    Item item = ForgeRegistries.ITEMS.getValue(name);
                    float modifier = obj.get("multiplier").getAsFloat();
                    wearablesProtection.put(item, modifier);
                    HordesLogger.logInfo("Loaded wearable protection " + name + " with modifier " + modifier);
                } catch (Exception e) {
                    HordesLogger.logError("Failed to load wearable protection " + element.toString(), e);
                }
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load wearable protection list", e);
        }
    }
    
    public ItemStack parseStack(JsonElement element) throws Exception {
        String name = element.getAsString();
        NBTTagCompound nbt = null;
        if (name.contains("{")) {
            String nbtstring = name.substring(name.indexOf("{"));
            name = name.substring(0, name.indexOf("{"));
            try {
                NBTTagCompound parsed = JsonToNBT.getTagFromJson(nbtstring);
                if (parsed != null) nbt = parsed;
            } catch (Exception e) {
                HordesLogger.logError("Error parsing nbt for item " + name + " " + e.getMessage(), e);
            }
        }
        String[] nameSplit = name.split(":");
        if (nameSplit.length >= 2) {
            ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
            int meta;
            try {
                meta = nameSplit.length > 2 ? (nameSplit[2].equals("*") ? OreDictionary.WILDCARD_VALUE : Integer.parseInt(nameSplit[2])) : 0;
            } catch (Exception e) {
                meta = 0;
                HordesLogger.logError("Entry" + name + " has a non integer, non wildcard metadata value", e);
            }
            if (ForgeRegistries.ITEMS.containsKey(loc)) {
                ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(loc), 1, meta);
                if (nbt != null) stack.setTagCompound(nbt);
                return stack;
            }
        }
        throw new Exception("Failed loading item " + name);
    }
    
    
    public List<ItemStack> getCureList() {
        return cures;
    }
    
    public boolean isCure(ItemStack stack) {
        for (ItemStack cure : cures) if (RecipeUtils.compareItemStacks(stack, cure, cure.getTagCompound() != null)) return true;
        return false;
    }
    
    public boolean canCauseInfection(Entity entity) {
        return infectionEntities.contains(entity.getClass());
    }

    public void tryToInfect(EntityLivingBase entity, EntityLiving attacker, DamageSource source, float amount) {
        if (MinecraftForge.EVENT_BUS.post(new InfectEntityEvent(entity, attacker, source, amount))) return;
        if ((entity instanceof EntityPlayer && InfectionConfig.infectPlayers))
            if (entity.getRNG().nextFloat() <= getModifiedInfectChance(entity, (float)(double)InfectionConfig.playerInfectChance)) PotionInfected.apply(entity);
        if ((entity instanceof EntityVillager && InfectionConfig.infectVillagers))
            if (entity.getRNG().nextFloat() <= getModifiedInfectChance(entity, (float)(double)InfectionConfig.villagerInfectChance)) PotionInfected.apply(entity);
        InfectionConversionEntry entry = conversionTable.get(entity.getClass());
        if (entry != null && entry.shouldInfect(entity)) PotionInfected.apply(entity);
    }

    public boolean canBeInfected(Entity entity) {
        if (entity instanceof EntityPlayer) return InfectionConfig.infectPlayers;
        if (entity instanceof EntityVillager && InfectionConfig.infectVillagers) return true;
        if (!(entity instanceof EntityLiving)) return false;
        return conversionTable.containsKey(entity.getClass());
    }

    public boolean convertEntity(EntityLivingBase entity) {
        InfectionConversionEntry entry = conversionTable.get(entity.getClass());
        if (entry != null) return entry.convertEntity(entity) != null;
        return false;
    }

    public boolean applyImmunity(EntityLivingBase entity, ItemStack stack) {
        for (Map.Entry<ItemStack, Integer> entry : immunityItems.entrySet()) {
            ItemStack cure = entry.getKey();
            if (RecipeUtils.compareItemStacks(stack, cure, cure.getTagCompound() != null)) {
                entity.addPotionEffect(new PotionEffect(HordesInfection.IMMUNITY, entry.getValue() * 20));
                return true;
            }
        }
        return false;
    }
    
    public float getModifiedInfectChance(EntityLivingBase entity, float chance) {
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR)
            chance *= getProtectionMultiplier(entity.getItemStackFromSlot(slot));
        return chance;
    }
    
    public float getProtectionMultiplier(ItemStack stack) {
        if (stack.isEmpty()) return 1;
        Item item = stack.getItem();
        return wearablesProtection.containsKey(item) ? wearablesProtection.get(item) : 1;
    }
    
    public void syncData(EntityPlayerMP player) {
        InfectionPacketHandler.sendTo(new SyncCuresMessage(cures), player);
        InfectionPacketHandler.sendTo(new SyncImmunityItemsMessage(immunityItems), player);
        InfectionPacketHandler.sendTo(new SyncWearableProtectionMessage(wearablesProtection), player);
    }

}
