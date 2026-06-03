package net.smileycorp.hordes.config.data.infection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
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
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.smileycorp.atlas.api.data.Pair;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.InfectEntityEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.data.HordesJsonLoader;
import net.smileycorp.hordes.config.data.HordesParsingException;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.PotionInfected;
import net.smileycorp.hordes.infection.network.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InfectionData extends HordesJsonLoader {

    public static InfectionData INSTANCE;
    
    private final List<ItemStack> cures = Lists.newArrayList();
    private final Map<Class<? extends EntityLivingBase>, InfectionConversionEntry> conversionTable = Maps.newHashMap();
    private final Map<ItemStack, Integer> immunityItems = Maps.newHashMap();
    private final Map<Item, Pair<Float, Byte>> wearablesProtection = Maps.newHashMap();
    private final Map<Class<? extends EntityLivingBase>, Float> entityInfectChance = Maps.newHashMap();
    
    public InfectionData(FMLPreInitializationEvent event) {
        super(new File(event.getModConfigurationDirectory().getPath() + "/hordes/infection"));
        INSTANCE = this;
    }

    @Override
    protected boolean shouldLoad() {
        return InfectionConfig.enableMobInfection;
    }

    @Override
    protected void dataInit() {}

    @Override
    protected void readData(Map<ResourceLocation, JsonElement> data) {
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING INFECTION CURES");
        cures.clear();
        try {
            for (JsonElement element : data.get(Constants.loc("infection_cures")).getAsJsonArray()) {
                try {
                    ItemStack stack = parseStack(element);
                    if (stack != null) {
                        cures.add(stack);
                        HordesLogger.logInfo("Loaded infection cure " + stack);
                    }
                } catch (Exception e) {
                    HordesLogger.logError("Failed to load cure entry " + element.toString(), e);
                }
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load infection cures", e);
        }
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING CONVERSION TABLE");
        try {
            HordesLogger.blankLine();
            JsonElement json = data.get(Constants.loc("infection_conversions"));
            for (JsonElement element : json.getAsJsonArray()) try {
                InfectionConversionEntry entry = InfectionConversionEntry.deserialize(element.getAsJsonObject());
                conversionTable.put((Class<? extends EntityLivingBase>) entry.getEntity().getEntityClass(), entry);
            } catch (Exception e) {
                HordesLogger.logError("Failed to load conversion entry " + element.toString(), e);
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load conversion table" + e.getMessage(), e);
        }
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING IMMUNITY ITEMS");
        try {
            for (JsonElement element : data.get(Constants.loc("immunity_items")).getAsJsonArray()) try {
                JsonObject obj = element.getAsJsonObject();
                ResourceLocation name = new ResourceLocation(obj.get("item").getAsString());
                ItemStack stack = parseStack(obj.get("item"));
                int duration = obj.get("duration").getAsInt();
                immunityItems.put(stack, duration);
                HordesLogger.logInfo("Loaded immunity item " + name + " with duration " + duration);
            } catch (Exception e) {
                HordesLogger.logError("Failed to load immunity item " + element.toString(), e);
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load immunity item list", e);
        }
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING WEARABLE PROTECTION LIST");
        try {
            for (JsonElement element : data.get(Constants.loc("wearables_protection")).getAsJsonArray()) try {
                JsonObject obj = element.getAsJsonObject();
                ResourceLocation name = new ResourceLocation(obj.get("item").getAsString());
                Item item = ForgeRegistries.ITEMS.getValue(name);
                float modifier = obj.get("protection").getAsFloat();
                if (item == null) throw new HordesParsingException("Invalid or missing item");
                wearablesProtection.put(item, Pair.of(modifier, obj.has("operation") ? getOperation(obj.get("operation").getAsString()) : (byte) 0));
                HordesLogger.logInfo("Loaded wearable protection " + name + " with modifier " + modifier);
            } catch (Exception e) {
                HordesLogger.logError("Failed to load wearable protection " + element.toString(), e);
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load wearable protection list", e);
        }
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING ENTITY INFECTION CHANCES");
        try {
            for (JsonElement element : data.get(Constants.loc("infection_entities")).getAsJsonArray()) try {
                JsonObject obj = element.getAsJsonObject();
                ResourceLocation name = new ResourceLocation(obj.get("entity").getAsString());
                EntityEntry entity = ForgeRegistries.ENTITIES.getValue(name);
                float chance = obj.get("chance").getAsFloat();
                entityInfectChance.put((Class<? extends EntityLivingBase>) entity.getEntityClass(), chance);
                HordesLogger.logInfo("Loaded infection entity " + name + " with infect chance " + chance);
            } catch (Exception e) {
                HordesLogger.logInfo("Failed to infection entity " + element.toString());
            }
        } catch (Exception e) {
            HordesLogger.logError("Failed to load entity infection list", e);
        }
    }

    //converts 1.21 modifier names used by the data files to their corresponding 1.12 operation integers
    private byte getOperation(String operation) {
        switch (operation.toLowerCase(Locale.US)) {
            case "add_multiplied_base":
                return 1;
            case "add_multiplied_total":
                return 2;
            default:
                return 0;
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

    @Override
    public void clearData() {
        cures.clear();
        conversionTable.clear();
        immunityItems.clear();
        wearablesProtection.clear();
        entityInfectChance.clear();
    }

    public List<ItemStack> getCureList() {
        return cures;
    }
    
    public boolean isCure(ItemStack stack) {
        for (ItemStack cure : cures) if (RecipeUtils.compareItemStacks(stack, cure, cure.getTagCompound() != null)) return true;
        return false;
    }

    public void tryToInfect(EntityLivingBase entity, EntityLivingBase attacker, DamageSource source, float amount) {
        if (MinecraftForge.EVENT_BUS.post(new InfectEntityEvent(entity, attacker, source, amount))) return;
        if (!canCauseInfection(attacker) |! canBeInfected(entity)) return;
        float r = attacker.getRNG().nextFloat();
        if (r <= getInfectionChance(entity, attacker)) PotionInfected.apply(entity);
            //if the entity is a player would the infection have succeeded if the player didn't have infection resistance?
            //if so send a protection sound message
        else if (entity instanceof EntityPlayerMP && r <= attacker.getEntityAttribute(HordesInfection.INFECTIVITY).getAttributeValue())
            InfectionPacketHandler.sendTo(new InfectMessage(true), (EntityPlayerMP) entity);
    }

    public boolean infectedTarget(Entity entity) {
        return !(entity instanceof EntityPlayer) && canBeInfected(entity);
    }

    public boolean canBeInfected(Entity entity) {
        if (entity instanceof EntityPlayer) return InfectionConfig.infectPlayers;
        if (!(entity instanceof EntityLiving)) return false;
        return conversionTable.containsKey(entity.getClass());
    }

    public boolean convertEntity(EntityLiving entity) {
        InfectionConversionEntry entry = conversionTable.get(entity.getClass());
        if (entry != null) return entry.convertEntity(entity) != null;
        return false;
    }

    public int getImmunityLength(ItemStack stack) {
        return immunityItems.getOrDefault(stack.getItem(), 0);
    }

    public boolean applyImmunity(EntityLivingBase entity, ItemStack stack) {
        for (Map.Entry<ItemStack, Integer> entry : immunityItems.entrySet()) if (RecipeUtils.compareItemStacks(stack, entry.getKey(), true)) {
            entity.addPotionEffect(new PotionEffect(HordesInfection.IMMUNITY, entry.getValue() * 20));
            return true;
        }
       return false;
    }

    public float getInfectionChance(Class<? extends Entity> clazz) {
        return entityInfectChance.containsKey(clazz) ? entityInfectChance.get(clazz) : 0;
    }

    public float getInfectionChance(EntityLivingBase entity, EntityLivingBase attacker) {
        return (float) attacker.getEntityAttribute(HordesInfection.INFECTIVITY).getAttributeValue()
                * (1 - (float) entity.getEntityAttribute(HordesInfection.INFECTION_RESISTANCE).getAttributeValue());
    }

    public float getProtection(Class<? extends Entity> clazz) {
        return EntityPlayer.class.isAssignableFrom(clazz) ? (float) InfectionConfig.playerInfectionResistance :
                conversionTable.containsKey(clazz) ? conversionTable.get(clazz).protection : 0;
    }

    @Nullable
    public Pair<Float, Byte> getProtection(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Item item = stack.getItem();
        return wearablesProtection.containsKey(item) ? wearablesProtection.get(item) : null;
    }

    @Deprecated
    public float getProtectionMultiplier(ItemStack stack) {
        Pair<Float, Byte> pair = getProtection(stack);
        return pair == null ? 0 : pair.getFirst();
    }

    public boolean hasInfectAttribute(Entity entity) {
        return entity instanceof EntityLivingBase && entityInfectChance.containsKey(entity.getClass());
    }

    public boolean canCauseInfection(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return false;
        IAttributeInstance attribute = ((EntityLivingBase) entity).getEntityAttribute(HordesInfection.INFECTIVITY);
        if (attribute == null) return false;
        return attribute.getAttributeValue() > 0;
    }
    
    public void syncData(EntityPlayerMP player) {
        InfectionPacketHandler.sendTo(new SyncCuresMessage(cures), player);
        InfectionPacketHandler.sendTo(new SyncImmunityItemsMessage(immunityItems), player);
        InfectionPacketHandler.sendTo(new SyncWearableProtectionMessage(wearablesProtection), player);
    }

}
