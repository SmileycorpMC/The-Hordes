package net.smileycorp.hordes.gibbing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public interface IZombieGibbing {

	boolean hasBodyPart(EnumBodyPart part);
	
	boolean hasBodyParts();

	void removePart(EnumBodyPart part);

	public static class Storage implements IStorage<IZombieGibbing> {

		@Override
		public NBTBase writeNBT(Capability<IZombieGibbing> capability, IZombieGibbing instance, EnumFacing side) {
			NBTTagCompound nbt = new NBTTagCompound();
			for (EnumBodyPart part : EnumBodyPart.values()) {
				nbt.setBoolean(part.name(), instance.hasBodyPart(part));
			}
			return nbt;
		}
	
		@Override
		public void readNBT(Capability<IZombieGibbing> capability,
				IZombieGibbing instance, EnumFacing side, NBTBase nbt) {
			if (nbt instanceof NBTTagCompound) {
				if (instance.hasBodyParts()) {
					for (EnumBodyPart part : EnumBodyPart.values()) {
						if (!((NBTTagCompound) nbt).getBoolean(part.name())) instance.removePart(part);
					}
				}
			}
		}
		
		
	}
	
	public static class Factory implements Callable<IZombieGibbing> {

		  @Override
		  public IZombieGibbing call() throws Exception {
		    return new Capabilty();
		  }
		  
	}
	
	public static class Capabilty implements IZombieGibbing {
		
		private List<EnumBodyPart> partList = new ArrayList<EnumBodyPart>();
		private Entity entity;
		
		public Capabilty(Entity entity) {
			this.entity=entity;
		}
		
		
		public Capabilty() {
			for (EnumBodyPart part : EnumBodyPart.values()) {
				partList.add(part);
			}
		}
		
		@Override
		public boolean hasBodyParts() {
			return !partList.isEmpty();
		}

		@Override
		public boolean hasBodyPart(EnumBodyPart part) {
			return partList.contains(part);
		}

		@Override
		public void removePart(EnumBodyPart part) {
			partList.remove(part);
			if (FMLCommonHandler.instance().getSide() == Side.SERVER && this.entity!=null) {
				GibbingPacketHandler.NETWORK_INSTANCE.sendToAll(new BodyPartMessage(entity));
			}
			
		}

	}
 
}
