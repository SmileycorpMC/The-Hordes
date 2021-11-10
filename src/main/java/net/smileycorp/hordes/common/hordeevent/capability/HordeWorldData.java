package net.smileycorp.hordes.common.hordeevent.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;

public class HordeWorldData extends WorldSavedData {

	public static final String DATA = ModDefinitions.MODID + "_HordeEvent";

	private int nextDay = 0;

	protected World world = null;

	public HordeWorldData() {
		this(DATA);
	}

	public HordeWorldData(String data) {
		super(data);
	}

	@Override
	public void load(CompoundNBT nbt) {
		if (nbt.contains("nextDay")) {
			int next = nbt.getInt("nextDay");
			if (nextDay == 0 || next > nextDay) {
				nextDay = next;
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt.putInt("nextDay", nextDay);
		return nbt;
	}

	public int getNextDay() {
		return nextDay;
	}

	public void setNextDay(int nextDay) {
		this.nextDay = nextDay;
	}

	public Map<PlayerEntity, OngoingHordeEvent> getEvents() {
		Map<PlayerEntity, OngoingHordeEvent> events = new HashMap<PlayerEntity, OngoingHordeEvent>();
		if (!world.isClientSide) {
			for (PlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent() &! player.isDeadOrDying()) {
					events.put(player, (OngoingHordeEvent) player.getCapability(Hordes.HORDE_EVENT, null).resolve().get());
				}
			}
		}
		return events;
	}

	public static HordeWorldData getData(ServerWorld world) {
		HordeWorldData data = (HordeWorldData) world.getChunkSource().getDataStorage().computeIfAbsent(() -> getCleanData(world), DATA);
		if (data.world == null) {
			data.world = world;
			int day = Math.round(world.getDayTime()/CommonConfigHandler.dayLength.get());
			if (!CommonConfigHandler.spawnFirstDay.get() && day <1) day  = 1;
			int multiplier = (int) Math.ceil(day / CommonConfigHandler.hordeSpawnDays.get());
			data.setNextDay((day * multiplier) + world.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1));
			data.setDirty();
		}
		return data;
	}

	public static HordeWorldData getCleanData(ServerWorld world) {
		HordeWorldData data = new HordeWorldData();
		data.world = world;
		int day = Math.round(world.getDayTime()/CommonConfigHandler.dayLength.get());
		double multiplier = Math.ceil(day / CommonConfigHandler.hordeSpawnDays.get());
		if (!(CommonConfigHandler.spawnFirstDay.get() && day == 0)) multiplier += 1;
		int nextDay = (int) Math.floor(((multiplier*CommonConfigHandler.hordeSpawnDays.get()) + world.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1)));
		data.setNextDay(nextDay);
		return data;
	}

	public List<String> getDebugText() {
		List<String> out = new ArrayList<String>();
		out.add(this.toString());
		out.add("Existing events: {");
		for (Entry<PlayerEntity,OngoingHordeEvent> entry : getEvents().entrySet()) {
			out.add("	" +entry.getValue().toString(entry.getKey()));
			out.addAll(entry.getValue().getEntityStrings());
		}
		out.add("}");
		return out;
	}

	@Override
	public String toString() {
		return super.toString() + "[worldTime: " + world.getDayTime() + ", nextDay="+nextDay+"]";
	}

}
