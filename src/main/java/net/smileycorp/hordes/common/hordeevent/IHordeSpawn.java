package net.smileycorp.hordes.common.hordeevent;

import java.util.concurrent.Callable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public interface IHordeSpawn {

	public boolean isHordeSpawned();
	
	public void setPlayerUUID(String uuid);
	
	public String getPlayerUUID();

	public static class Storage implements IStorage<IHordeSpawn> {

		@Override
		public NBTBase writeNBT(Capability<IHordeSpawn> capability, IHordeSpawn instance, EnumFacing side) {
			return new NBTTagString(instance.getPlayerUUID());
		}
	
		@Override
		public void readNBT(Capability<IHordeSpawn> capability, IHordeSpawn instance, EnumFacing side, NBTBase nbt) {
				instance.setPlayerUUID(((NBTTagString) nbt).getString());
		}
		
		
	}
	
	public static class Factory implements Callable<IHordeSpawn> {

		  @Override
		  public IHordeSpawn call() throws Exception {
		    return new Capabilty();
		  }
		  
	}
	
	public static class Capabilty implements IHordeSpawn {

		private String uuid = "";
		
		@Override
		public boolean isHordeSpawned() {
			return !uuid.isEmpty();
		}

		@Override
		public void setPlayerUUID(String uuid) {
			this.uuid=uuid;
		}

		@Override
		public String getPlayerUUID() {
			return uuid;
		}

	}
 
}
