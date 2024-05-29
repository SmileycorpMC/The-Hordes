package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public interface HordeEventClient {
	
	boolean isHordeNight(World level);
	
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
		public boolean isHordeNight(World level) {
			if (day_length == 0 |! horde_day) return false;
			return (level.getWorldTime() % day_length >= 0.5 * day_length);
		}
		
	}
	
	class Provider implements ICapabilityProvider {
		
		protected HordeEventClient instance = new Impl();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == HordesCapabilities.HORDE_EVENT_CLIENT;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == HordesCapabilities.HORDE_EVENT_CLIENT ? HordesCapabilities.HORDE_EVENT_CLIENT.cast(instance) : null;
		}

	}
 
}
