package net.smileycorp.hordes.hordeevent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

import java.util.Locale;

public class HordeSpawnData {
    
    private HordeSpawnTable table = HordeTableLoader.INSTANCE.getFallbackTable();
    private HordeSpawnType spawnType = HordeSpawnType.AVOID_FLUIDS;
    private ResourceLocation spawnSound = Constants.HORDE_SOUND;
    private String startMessage = Constants.hordeEventStart;
    
    private String endMessage = Constants.hordeEventEnd;
    
    public HordeSpawnData() {};
    
    public HordeSpawnData(CompoundTag tag) {
        if (tag.contains("table")) table = HordeTableLoader.INSTANCE.getTable(new ResourceLocation(tag.getString("table")));
        if (tag.contains("spawnType")) spawnType = HordeSpawnType.valueOf(tag.getString("spawnType").toUpperCase(Locale.US));
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
    
}
