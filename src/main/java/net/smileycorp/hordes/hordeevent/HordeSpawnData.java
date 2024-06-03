package net.smileycorp.hordes.hordeevent;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.data.hordeevent.HordeTableLoader;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeSpawnData {
    
    private HordeSpawnTable table = HordeTableLoader.INSTANCE.getFallbackTable();
    private HordeSpawnType spawnType = HordeSpawnTypes.AVOID_FLUIDS;
    private ResourceLocation spawnSound = Constants.HORDE_SOUND;
    private String startMessage = Constants.hordeEventStart;
    private String endMessage = Constants.hordeEventEnd;
    private int spawnDuration = HordeEventConfig.hordeSpawnDuration;
    private int spawnInterval = HordeEventConfig.hordeSpawnInterval;
    private int spawnAmount;
    
    private double entitySpeed = HordeEventConfig.hordeEntitySpeed;
    
    public HordeSpawnData(HordeEvent horde) {
        spawnAmount = (int) (HordeEventConfig.hordeSpawnAmount * (1 + (horde.getDay() / HordeEventConfig.hordeSpawnDays)
                * (HordeEventConfig.hordeSpawnMultiplier - 1)));
    }
    
    public HordeSpawnData(HordeEvent horde, NBTTagCompound tag) {
        this(horde);
        if (tag.hasKey("table")) table = HordeTableLoader.INSTANCE.getTable(new ResourceLocation(tag.getString("table")));
        if (tag.hasKey("spawnType")) spawnType = HordeSpawnTypes.fromNBT(tag.getTag("spawnType"));
        if (tag.hasKey("spawnSound")) spawnSound = new ResourceLocation(tag.getString("spawnSound"));
        if (tag.hasKey("startMessage")) startMessage = tag.getString("startMessage");
        if (tag.hasKey("endMessage")) endMessage = tag.getString("endMessage");
        if (tag.hasKey("spawnDuration")) spawnDuration = tag.getInteger("spawnDuration");
        if (tag.hasKey("spawnInterval")) spawnInterval = tag.getInteger("spawnInterval");
        if (tag.hasKey("spawnAmount")) spawnAmount = tag.getInteger("spawnAmount");
        if (tag.hasKey("entitySpeed")) entitySpeed = tag.getDouble("entitySpeed");
    }
    
    public NBTTagCompound save() {
        NBTTagCompound tag = new NBTTagCompound();
        if (table != null) tag.setString("table", table.getName().toString());
        if (spawnType != null) tag.setTag("spawnType", HordeSpawnTypes.toNbt(spawnType));
        if (spawnSound != null) tag.setString("spawnSound", spawnSound.toString());
        if (startMessage != null) tag.setString("startMessage", startMessage);
        if (endMessage != null) tag.setString("endMessage", endMessage);
        tag.setInteger("spawnDuration", spawnDuration);
        tag.setInteger("spawnInterval", spawnInterval);
        tag.setInteger("spawnAmount", spawnAmount);
        tag.setDouble("entitySpeed", entitySpeed);
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
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName()+"[");
        builder.append("table=" + table.getName() + ", ");
        builder.append("spawnType=" + HordeSpawnTypes.toString(spawnType) + ", ");
        builder.append("spawnSound=" + spawnSound + ", ");
        builder.append("startMessage=" + startMessage + ", ");
        builder.append("endMessage=" + endMessage + ", ");
        builder.append("spawnDuration=" + spawnDuration + ", ");
        builder.append("spawnInterval=" + spawnInterval + ", ");
        builder.append("spawnAmount=" + spawnAmount + ", ");
        builder.append("entitySpeed=" + entitySpeed + "]");
        return builder.toString();
    }
    
}
