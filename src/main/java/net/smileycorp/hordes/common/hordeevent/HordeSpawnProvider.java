package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class HordeSpawnProvider implements ICapabilitySerializable<NBTBase> {
		
		@CapabilityInject(IHordeSpawn.class)
		public final static Capability<IHordeSpawn> HORDESPAWN = null;
		
		protected IHordeSpawn instance = HORDESPAWN.getDefaultInstance();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == HORDESPAWN;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == HORDESPAWN ? HORDESPAWN.cast(instance) : null;
		}

		@Override
		public NBTBase serializeNBT() {
			return HORDESPAWN.getStorage().writeNBT(HORDESPAWN, instance, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			HORDESPAWN.getStorage().readNBT(HORDESPAWN, instance, null, nbt);
		}

}
