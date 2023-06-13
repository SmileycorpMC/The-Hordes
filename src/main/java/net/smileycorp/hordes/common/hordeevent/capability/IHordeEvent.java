package net.smileycorp.hordes.common.hordeevent.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.atlas.api.IOngoingEvent;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnTable;

public interface IHordeEvent extends IOngoingEvent<Player> {

	public void spawnWave(Player player, int count);

	public boolean isHordeDay(Player player);

	public boolean hasChanged();

	public void setPlayer(Player player);

	public void tryStartEvent(Player player, int duration, boolean isCommand);

	public void setSpawntable(HordeSpawnTable table);

	public void setNextDay(int day);

	public int getNextDay();

	public void stopEvent(Player player, boolean isCommand);

	public void removeEntity(Mob entity);

	public void registerEntity(Mob entity);

	public void reset(ServerLevel world);

	public static IHordeEvent createEvent() {
		return new HordeEvent();
	}

	public static class Provider implements ICapabilitySerializable<CompoundTag> {

		protected IHordeEvent impl = new HordeEvent();

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == Hordes.HORDE_EVENT ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public CompoundTag serializeNBT() {
			return impl.writeToNBT(new CompoundTag());
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			impl.readFromNBT(nbt);
		}

	}

}
