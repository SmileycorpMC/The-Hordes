package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

import java.util.UUID;

public interface HordeSpawn {
	
	boolean isHordeSpawned();
	
	void setPlayerUUID(String uuid);
	
	String getPlayerUUID();
	
	boolean isSynced();
	
	void setSynced();
	
	NBTTagString writeNBT();
	
	void readNBT(NBTTagString tag);
	
	static EntityPlayerMP getHordePlayer(Entity entity) {
		if (entity.world.isRemote |!(entity instanceof EntityLiving)) return null;
		if (!entity.hasCapability(HordesCapabilities.HORDESPAWN, null)) return null;
		HordeSpawn cap = entity.getCapability(HordesCapabilities.HORDESPAWN, null);
		if (!cap.isHordeSpawned()) return null;
		String uuid = cap.getPlayerUUID();
		if (!DataUtils.isValidUUID(uuid)) return null;
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
	}
	
	class Impl implements HordeSpawn {
		
		private String uuid = "";
		private boolean isSynced;
		
		@Override
		public boolean isHordeSpawned() {
			return !uuid.isEmpty();
		}
		
		@Override
		public void setPlayerUUID(String uuid) {
			this.uuid = uuid;
		}
		
		@Override
		public String getPlayerUUID() {
			return uuid;
		}
		
		@Override
		public boolean isSynced() {
			return isSynced;
		}
		
		@Override
		public void setSynced() {
			isSynced = true;
		}
		
		@Override
		public NBTTagString writeNBT() {
			return new NBTTagString(uuid);
		}
		
		@Override
		public void readNBT(NBTTagString tag) {
			uuid = tag.getString();
		}
		
	}

	class Storage implements IStorage<HordeSpawn> {

		@Override
		public NBTBase writeNBT(Capability<HordeSpawn> capability, HordeSpawn instance, EnumFacing side) {
			return instance.writeNBT();
		}
	
		@Override
		public void readNBT(Capability<HordeSpawn> capability, HordeSpawn instance, EnumFacing side, NBTBase nbt) {
			instance.readNBT((NBTTagString) nbt);
		}
		
		
	}
	
	class Provider implements ICapabilitySerializable<NBTBase> {
		
		protected HordeSpawn instance = new Impl();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == HordesCapabilities.HORDESPAWN;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == HordesCapabilities.HORDESPAWN ? HordesCapabilities.HORDESPAWN.cast(instance) : null;
		}

		@Override
		public NBTBase serializeNBT() {
			return HordesCapabilities.HORDESPAWN.getStorage().writeNBT(HordesCapabilities.HORDESPAWN, instance, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			HordesCapabilities.HORDESPAWN.getStorage().readNBT(HordesCapabilities.HORDESPAWN, instance, null, nbt);
		}

	}
 
}
