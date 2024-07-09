package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public interface HordeEventClient {
    
    boolean isHordeNight(Level level);
    
    void setHordeDay(boolean hordeDay, int day_length);
    
    class Impl implements HordeEventClient {
        
        private int day_length;
        private boolean horde_day;
        
        @Override
        public void setHordeDay(boolean hordeDay, int day_length) {
            if (day_length > 0) this.day_length = day_length;
            this.horde_day = hordeDay;
        }
        
        @Override
        public boolean isHordeNight(Level level) {
            if (day_length == 0 |! horde_day) return false;
            return (level.getDayTime() % day_length >= 0.5 * day_length);
        }
        
    }
    
    class Provider implements ICapabilityProvider {
        
        protected HordeEventClient impl = new HordeEventClient.Impl();
        
        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
            return cap == HordesCapabilities.HORDE_EVENT_CLIENT ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
        }
        
    }
    
}
