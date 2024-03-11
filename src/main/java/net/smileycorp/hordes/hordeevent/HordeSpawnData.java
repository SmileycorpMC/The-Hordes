package net.smileycorp.hordes.hordeevent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

import java.util.Locale;

public class HordeSpawnData {
    
    private HordeSpawnTable table = HordeTableLoader.INSTANCE.getFallbackTable();
    private HordeSpawnType spawnType = HordeSpawnTypes.AVOID_FLUIDS;
    private ResourceLocation spawnSound = Constants.HORDE_SOUND;
    private String startMessage = Constants.hordeEventStart;
    private String endMessage = Constants.hordeEventEnd;
    private int spawnDuration = HordeEventConfig.hordeSpawnDuration.get();
    private int spawnInterval = HordeEventConfig.hordeSpawnInterval.get();
    private int spawnAmount;
    
    private double entitySpeed = HordeEventConfig.hordeEntitySpeed.get();
    
    public HordeSpawnData(HordeEvent horde) {
        spawnAmount = (int) (HordeEventConfig.hordeSpawnAmount.get() * (1 + (horde.getDay() / HordeEventConfig.hordeSpawnDays.get())
                * (HordeEventConfig.hordeSpawnMultiplier.get() - 1)));
    }
    
    public HordeSpawnData(HordeEvent horde, CompoundTag tag) {
        this(horde);
        if (tag.contains("table")) table = HordeTableLoader.INSTANCE.getTable(new ResourceLocation(tag.getString("table")));
        if (tag.contains("spawnType")) spawnType = HordeSpawnTypes.fromNBT(tag.get("spawnType"));
        if (tag.contains("spawnSound")) spawnSound = new ResourceLocation(tag.getString("spawnSound"));
        if (tag.contains("startMessage")) startMessage = tag.getString("startMessage");
        if (tag.contains("endMessage")) endMessage = tag.getString("endMessage");
    }
    
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (table != null) tag.putString("table", table.getName().toString());
        if (spawnType != null) tag.putString("spawnType", spawnType.toString().toLowerCase(Locale.US));
        if (spawnSound != null) tag.putString("spawnSound", spawnSound.toString());
        if (startMessage != null) tag.putString("startMessage", startMessage);
        if (endMessage != null) tag.putString("endMessage", endMessage);
        return tag;
    }
    
    public HordeSpawnTable getTable() {
        return table;
    }
    
    public void setTable(HordeSpawnTable table) {
        this.table = table;
    }
    
    public HordeSpawnType getSpawnType() {
        return spawnType;
    }
    
    public void setSpawnType(HordeSpawnType spawnType) {
        this.spawnType = spawnType;
    }
    
    public ResourceLocation getSpawnSound() {
        return spawnSound;
    }
    
    public void setSpawnSound(ResourceLocation spawnSound) {
        this.spawnSound = spawnSound;
    }
    
    public String getStartMessage() {
        return startMessage;
    }
    
    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }
    
    public String getEndMessage() {
        return endMessage;
    }
    
    public void setEndMessage(String endMessage) {
        this.endMessage = endMessage;
    }
    
    public int getSpawnDuration() {
        return spawnDuration;
    }
    
    public void setSpawnDuration(int spawnDuration) {
        this.spawnDuration = spawnDuration;
    }
    
    public int getSpawnInterval() {
        return spawnInterval;
    }
    
    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = spawnInterval;
    }
    
    public int getSpawnAmount() {
        return spawnAmount;
    }
    
    public void setSpawnAmount(int spawnAmount) {
        this.spawnAmount = spawnAmount;
    }
    
    public double getEntitySpeed() {
        return entitySpeed;
    }
    
    public void setEntitySpeed(double entitySpeed) {
        this.entitySpeed = entitySpeed;
    }
    
}
