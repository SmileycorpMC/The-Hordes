package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.smileycorp.atlas.api.IOngoingEvent;
import net.smileycorp.hordes.common.Hordes;

public interface IOngoingHordeEvent extends IOngoingEvent {
	
	public void spawnWave(World world, int count);

	public boolean isHordeDay(World world);
	
	public boolean hasChanged();
	
	public EntityPlayer getPlayer();
	
	public void setPlayer(EntityPlayer player);

	public void tryStartEvent(int duration, boolean isCommand);
	
	public void setNextDay(int day);
	
	public int getNextDay();

	public void stopEvent(World world, boolean isCommand);

	public void removeEntity(EntityLiving entity);

	public void registerEntity(EntityLiving entity);
	
	public static class Storage implements IStorage<IOngoingHordeEvent> {

		@Override
		public NBTBase writeNBT(Capability<IOngoingHordeEvent> capability, IOngoingHordeEvent instance, EnumFacing side) {
			return instance.writeToNBT(new NBTTagCompound());
		}

		@Override
		public void readNBT(Capability<IOngoingHordeEvent> capability, IOngoingHordeEvent instance, EnumFacing side, NBTBase nbt) {
			instance.readFromNBT((NBTTagCompound) nbt);
		}
		
	}
	
	public static class Provider implements ICapabilitySerializable<NBTBase> {
		
		protected IOngoingHordeEvent instance;
		
		public Provider(EntityPlayer player) {
			instance = new OngoingHordeEvent(player.world.isRemote ? player.world : FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0), player);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == Hordes.HORDE_EVENT;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == Hordes.HORDE_EVENT ? Hordes.HORDE_EVENT.cast(instance) : null;
		}

		@Override
		public NBTBase serializeNBT() {
			return Hordes.HORDE_EVENT.getStorage().writeNBT(Hordes.HORDE_EVENT, instance, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			Hordes.HORDE_EVENT.getStorage().readNBT(Hordes.HORDE_EVENT, instance, null, nbt);
		}

	}
}
