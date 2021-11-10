package net.smileycorp.hordes.common.hordeevent.capability;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.atlas.api.IOngoingEvent;
import net.smileycorp.hordes.common.Hordes;

public interface IOngoingHordeEvent extends IOngoingEvent<PlayerEntity> {

	public void spawnWave(PlayerEntity player, int count);

	public boolean isHordeDay(PlayerEntity player);

	public boolean hasChanged();

	public void setPlayer(PlayerEntity player);

	public void tryStartEvent(PlayerEntity player, int duration, boolean isCommand);

	public void setNextDay(int day);

	public int getNextDay();

	public void stopEvent(PlayerEntity player, boolean isCommand);

	public void removeEntity(MobEntity entity);

	public void registerEntity(MobEntity entity);

	public void reset(ServerWorld world);

	public static class Storage implements IStorage<IOngoingHordeEvent> {

		@Override
		public INBT writeNBT(Capability<IOngoingHordeEvent> capability, IOngoingHordeEvent instance, Direction side) {
			return instance.writeToNBT(new CompoundNBT());
		}

		@Override
		public void readNBT(Capability<IOngoingHordeEvent> capability, IOngoingHordeEvent instance, Direction side, INBT nbt) {
			instance.readFromNBT((CompoundNBT) nbt);
		}

	}

	public static class Provider implements ICapabilitySerializable<INBT> {

		protected IOngoingHordeEvent impl = new OngoingHordeEvent();

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
