package net.smileycorp.hordes.infection.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.HordesJsonLoader;
import net.smileycorp.hordes.common.data.HordesParsingException;
import net.smileycorp.hordes.common.event.InfectEntityEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectedEffect;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;
import net.smileycorp.hordes.infection.network.SyncImmunityItemsMessage;
import net.smileycorp.hordes.infection.network.SyncWearableProtectionMessage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class InfectionData extends HordesJsonLoader {

    public static InfectionData INSTANCE = new InfectionData();

    private final Map<EntityType<?>, InfectionConversionEntry> conversionTable = Maps.newHashMap();
    private final Map<Item, Integer> immunityItems = Maps.newHashMap();
    private final Map<Item, Pair<Float, AttributeModifier.Operation>> wearablesProtection = Maps.newHashMap();
    private final Map<EntityType<?>, Float> entityInfectChance = Maps.newHashMap();

    public InfectionData() {
        super("infection");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING CONVERSION TABLE");
        conversionTable.clear();
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = ResourceLocation.tryBuild(id, "infection_conversions");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.blankLine();
                HordesLogger.logInfo("Loading conversion table " + loc);
                for (JsonElement element : json.getAsJsonArray()) try {
                    InfectionConversionEntry entry = InfectionConversionEntry.deserialize(element.getAsJsonObject());
                    conversionTable.put(entry.getEntity(), entry);
                } catch (Exception e) {
                    HordesLogger.logError("Failed to load conversion entry " + element.toString(), e);
                }
            } catch (Exception e) {
                HordesLogger.logError("Failed to load conversion table " + loc, e);
            }
        }
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING IMMUNITY ITEMS");
        immunityItems.clear();
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = ResourceLocation.tryBuild(id, "immunity_items");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.blankLine();
                HordesLogger.logInfo("Loading immunity item list " + loc);
                for (JsonElement element : json.getAsJsonArray()) try {
                    JsonObject obj = element.getAsJsonObject();
                    ResourceLocation name = ResourceLocation.tryParse(obj.get("item").getAsString());
                    Item item = BuiltInRegistries.ITEM.get(name);
                    int duration = obj.get("duration").getAsInt();
                    immunityItems.put(item, duration);
                    HordesLogger.logInfo("Loaded immunity item " + name + " with duration " + duration);
                } catch (Exception e) {
                    HordesLogger.logError("Failed to load immunity item " + element.toString(), e);
                }
            } catch (Exception e) {
                HordesLogger.logError("Failed to load immunity item list " + loc, e);
            }
        }
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING WEARABLE PROTECTION LIST");
        wearablesProtection.clear();
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = ResourceLocation.tryBuild(id, "wearables_protection");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.blankLine();
                HordesLogger.logInfo("Loading wearables protection list " + loc);
                for (JsonElement element : json.getAsJsonArray()) try {
                    JsonObject obj = element.getAsJsonObject();
                    ResourceLocation name = ResourceLocation.tryParse(obj.get("item").getAsString());
                    Item item = BuiltInRegistries.ITEM.get(name);
                    float modifier = obj.get("protection").getAsFloat();
                    if (item == null || item == Items.AIR) throw new HordesParsingException("Invalid or missing item");
                    AttributeModifier.Operation operation = null;
                    if (obj.has("operation")) operation = ((StringRepresentable.EnumCodec<AttributeModifier.Operation>)AttributeModifier.Operation.CODEC)
                            .byName(obj.get("operation").getAsString());
                    wearablesProtection.put(item, Pair.of(modifier, operation == null ?
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL : operation));
                    HordesLogger.logInfo("Loaded wearable protection " + name + " with modifier " + modifier);
                } catch (Exception e) {
                    HordesLogger.logError("Failed to load wearable protection " + element.toString(), e);
                }
            } catch (Exception e) {
                HordesLogger.logError("Failed to load wearable protection list " + loc, e);
            }
        }
        entityInfectChance.clear();
        HordesLogger.blankLine();
        HordesLogger.heading("LOADING ENTITY INFECTION CHANCES");
        for (String id : manager.getNamespaces()) {
            ResourceLocation loc = ResourceLocation.tryBuild(id, "infection_entities");
            JsonElement json = map.get(loc);
            if (json == null) continue;
            try {
                HordesLogger.logInfo("Loading entity infection list " + loc);
                for (JsonElement element : json.getAsJsonArray()) try {
                    JsonObject obj = element.getAsJsonObject();
                    ResourceLocation name = ResourceLocation.tryParse(obj.get("entity").getAsString());
                    EntityType<?> entity = BuiltInRegistries.ENTITY_TYPE.get(name);
                    float chance = obj.get("chance").getAsFloat();
                    entityInfectChance.put(entity, chance);
                    HordesLogger.logInfo("Loaded infection entity " + name + " with infect chance " + chance);
                } catch (Exception e) {
                    HordesLogger.logError("Failed to infection entity " + element.toString(), e);
                }
            } catch (Exception e) {
                HordesLogger.logError("Failed to load entity infection list " + loc, e);
            }
        }
    }

    public void tryToInfect(LivingEntity entity, LivingEntity attacker, DamageSource source, float amount) {
        if (NeoForge.EVENT_BUS.post(new InfectEntityEvent(entity, attacker, source, amount)).isCanceled()) return;
        if (!canCauseInfection(attacker) |! canBeInfected(entity)) return;
        float r = attacker.getRandom().nextFloat();
        if (r <= getInfectionChance(entity, attacker)) InfectedEffect.apply(entity);
            //if the entity is a player would the infection have succeeded if the player didn't have infection resistance?
            //if so send a protection sound message
        else if (entity instanceof ServerPlayer && r <= attacker.getAttribute(HordesInfection.INFECTIVITY).getValue())
            InfectionPacketHandler.sendTo(new InfectMessage(true), (ServerPlayer) entity);
    }

    public boolean infectedTarget(Entity entity) {
        return !(entity instanceof Player) && canBeInfected(entity);
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
        return immunityItems.getOrDefault(stack.getItem(), 0);
    }

    public boolean applyImmunity(LivingEntity entity, Item item) {
        if (!immunityItems.containsKey(item)) return false;
        entity.addEffect(new MobEffectInstance(HordesInfection.IMMUNITY, immunityItems.get(item) * 20));
        return true;
    }

    public float getInfectionChance(EntityType<?> entity) {
        return entityInfectChance.containsKey(entity) ? entityInfectChance.get(entity) : 0;
    }

    public float getInfectionChance(LivingEntity entity, LivingEntity attacker) {
        return (float) attacker.getAttributeValue(HordesInfection.INFECTIVITY)
                * (1 - (float) entity.getAttributeValue(HordesInfection.INFECTION_RESISTANCE));
    }

    public float getProtection(EntityType<?> type) {
        return type == EntityType.PLAYER ? (float) (double) InfectionConfig.playerInfectionResistance.get() :
                conversionTable.containsKey(type) ? conversionTable.get(type).protection : 0;
    }

    @Nullable
    public Pair<Float, AttributeModifier.Operation> getProtection(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Item item = stack.getItem();
        return wearablesProtection.containsKey(item) ? wearablesProtection.get(item) : null;
    }

    @Deprecated
    public float getProtectionMultiplier(ItemStack stack) {
        Pair<Float, AttributeModifier.Operation> pair = getProtection(stack);
        return pair == null ? 0 : pair.getFirst();
    }

    public boolean canCauseInfection(EntityType<?> entity) {
        return entityInfectChance.containsKey(entity);
    }

    public boolean hasInfectAttribute(Entity entity) {
        return entity instanceof LivingEntity && entityInfectChance.containsKey(entity.getType());
    }

    public boolean canCauseInfection(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        if (!((LivingEntity) entity).getAttributes().hasAttribute(HordesInfection.INFECTIVITY)) return false;
        return ((LivingEntity) entity).getAttribute(HordesInfection.INFECTIVITY).getValue() > 0;
    }

    public void syncData(ServerPlayer player) {
        InfectionPacketHandler.sendTo(new SyncImmunityItemsMessage(immunityItems), player);
        InfectionPacketHandler.sendTo(new SyncWearableProtectionMessage(wearablesProtection), player);
    }

    public void readImmunityItems(List<Map.Entry<Item, Integer>> data) {
        immunityItems.clear();
        data.forEach(e -> immunityItems.put(e.getKey(), e.getValue()));
    }

    public void readWearableProtection(List<Pair<Item, Pair<Float, AttributeModifier.Operation>>> data) {
        wearablesProtection.clear();
        data.forEach(e -> wearablesProtection.put(e.getFirst(), e.getSecond()));
    }

    public void clear() {
        immunityItems.clear();
        wearablesProtection.clear();
    }

}
