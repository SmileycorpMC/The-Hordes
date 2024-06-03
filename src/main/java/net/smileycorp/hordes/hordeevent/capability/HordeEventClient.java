package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

import javax.annotation.Nullable;

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
			if (level.getWorldTime() % day_length < 0.5 * day_length) return false;
			return level.getWorldTime() > day * day_length;
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
    
    class Storage implements Capability.IStorage<HordeEventClient> {
	
		@Nullable
		@Override
		public NBTBase writeNBT(Capability<HordeEventClient> capability, HordeEventClient instance, EnumFacing side) {
			return null;
		}
	
		@Override
		public void readNBT(Capability<HordeEventClient> capability, HordeEventClient instance, EnumFacing side, NBTBase nbt) {}
		
	}
}
