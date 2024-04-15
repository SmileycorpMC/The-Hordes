package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public interface HordeEventClient {

    void setNextDay(int day, int day_length);

    boolean isHordeNight(World level);

    class Impl implements HordeEventClient {

        private int day, day_length;

        @Override
        public void setNextDay(int day, int day_length) {
            if (day_length > 0) this.day_length = day_length;
            this.day = day;
        }

        @Override
        public boolean isHordeNight(World level) {
            if (day_length == 0) return false;
            if (level.getDayTime() % day_length < 0.5 * day_length) return false;
            return level.getDayTime() > day * day_length;
        }

    }

    class Provider implements ICapabilityProvider {

        protected HordeEventClient impl = new Impl();

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
            return cap == HordesCapabilities.HORDE_EVENT_CLIENT ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
        }

    }

}
