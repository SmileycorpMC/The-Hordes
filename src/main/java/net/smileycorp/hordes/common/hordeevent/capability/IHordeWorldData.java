package net.smileycorp.hordes.common.hordeevent.capability;

import java.util.Set;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.hordes.common.Hordes;

public interface IHordeWorldData {


	public int getNextDay();

	public void setNextDay(int nextDay);

	public Set<OngoingHordeEvent> getEvents();

	public void readFromNBT(CompoundNBT nbt);

	public CompoundNBT writeToNBT(CompoundNBT nbt);

	public static class Storage implements IStorage<IHordeWorldData> {

		@Override
		public INBT writeNBT(Capability<IHordeWorldData> capability, IHordeWorldData instance, Direction side) {
			return instance.writeToNBT(new CompoundNBT());
		}

		@Override
		public void readNBT(Capability<IHordeWorldData> capability, IHordeWorldData instance, Direction side, INBT nbt) {
			instance.readFromNBT((CompoundNBT) nbt);
		}

	}

	public static class Provider implements ICapabilitySerializable<INBT> {

		protected IHordeWorldData impl;

		public Provider(PlayerEntity player) {
			impl = new OngoingHordeEvent(player.level.isClientSide ? player.level : ServerLifecycleHooks.getCurrentServer().overworld(), player);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == Hordes.HORDE_EVENT ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public INBT serializeNBT() {
			return Hordes.HORDE_EVENT.getStorage().writeNBT(Hordes.HORDE_EVENT, impl, null);
		}

		@Override
		public void deserializeNBT(INBT nbt) {
			Hordes.HORDE_EVENT.getStorage().readNBT(Hordes.HORDE_EVENT, impl, null, nbt);
		}

	}
}
