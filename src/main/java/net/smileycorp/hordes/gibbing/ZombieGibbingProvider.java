package net.smileycorp.hordes.gibbing;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ZombieGibbingProvider implements ICapabilitySerializable {
		
		@CapabilityInject(IZombieGibbing.class)
		public final static Capability<IZombieGibbing> GIBBING = null;
		
		private Entity entity;

		public ZombieGibbingProvider(Entity entity) {
			this.entity = entity;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability instanceof IZombieGibbing;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability.getDefaultInstance();
		}

		@Override
		public NBTBase serializeNBT() {
			return GIBBING.getStorage().writeNBT(GIBBING, new IZombieGibbing.Capabilty(entity), EnumFacing.UP);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			GIBBING.getStorage().readNBT(GIBBING, new IZombieGibbing.Capabilty(entity), EnumFacing.UP, nbt);
		}

}
