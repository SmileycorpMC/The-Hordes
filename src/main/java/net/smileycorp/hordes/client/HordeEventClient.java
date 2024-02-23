package net.smileycorp.hordes.client;

import net.minecraft.world.level.Level;

public class HordeEventClient {
    
    private static HordeEventClient INSTANCE = new HordeEventClient();

    private int day, day_length;
    
    public void setNextDay(int day, int day_length) {
        if (day_length > 0) this.day_length = day_length;
        this.day = day;
    }
    
    public boolean isHordeNight(Level level) {
        if (day_length == 0) return false;
        if (level.getDayTime() % day_length < 0.5 * day_length) return false;
        return level.getDayTime() > day * day_length;
    }
    
    public static HordeEventClient getInstance() {
        return INSTANCE;
    }

}
