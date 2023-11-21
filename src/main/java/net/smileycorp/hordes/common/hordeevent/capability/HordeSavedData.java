package net.smileycorp.hordes.common.hordeevent.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HordeSavedData extends SavedData {

	public static final String DATA = Constants.MODID + "_HordeEvent";

	private int nextDay = 0;

	protected Level level = null;

	public void load(CompoundTag nbt) {
		if (nbt.contains("nextDay")) {
			int next = nbt.getInt("nextDay");
			if (next > nextDay) {
				nextDay = next;
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt.putInt("nextDay", nextDay);
		return nbt;
	}

	public int getNextDay() {
		return nextDay;
	}

	public void setNextDay(int nextDay) {
		this.nextDay = nextDay;
	}

	public Map<Player, HordeEvent> getEvents() {
		Map<Player, HordeEvent> events = new HashMap<>();
		if (!level.isClientSide) {
			for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent() &! player.isDeadOrDying()) {
					events.put(player, (HordeEvent) player.getCapability(Hordes.HORDE_EVENT, null).resolve().get());
				}
			}
		}
		return events;
	}

	public void save() {
		setDirty();
		if (level instanceof ServerLevel) ((ServerLevel)level).getChunkSource().getDataStorage().set(DATA, this);
	}

	public List<String> getDebugText() {
		List<String> out = new ArrayList<>();
		out.add(toString());
		out.add("Existing events: {");
		for (Entry<Player, HordeEvent> entry : getEvents().entrySet()) {
			out.add("	" +entry.getValue().toString(entry.getKey()));
			out.addAll(entry.getValue().getEntityStrings());
		}
		out.add("}");
		return out;
	}

	@Override
	public String toString() {
		return super.toString() + "[levelTime: " + level.getDayTime() + ", nextDay="+nextDay+"]";
	}

	public static HordeSavedData getData(ServerLevel level) {
		HordeSavedData data = (HordeSavedData) level.getChunkSource().getDataStorage().computeIfAbsent((nbt) -> getDataFromNBT(level, nbt), () -> getCleanData(level), DATA);
		if (data == null) {
			data = getCleanData(level);
		}
		level.getChunkSource().getDataStorage().set(DATA, data);
		return data;
	}

	private static HordeSavedData getDataFromNBT(ServerLevel level, CompoundTag nbt) {
		HordeSavedData data = getCleanData(level);
		data.load(nbt);
		return data;
	}

	public static HordeSavedData getCleanData(ServerLevel level) {
		HordeSavedData data = new HordeSavedData();
		data.level = level;
		int day = Math.round(level.getDayTime() / CommonConfigHandler.dayLength.get());
		double multiplier = Math.ceil(day / CommonConfigHandler.hordeSpawnDays.get());
		if (!(CommonConfigHandler.spawnFirstDay.get() && day == 0)) multiplier += 1;
		int nextDay = (int) Math.floor(((multiplier * CommonConfigHandler.hordeSpawnDays.get()) + level.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1)));
		data.setNextDay(nextDay);
		return data;
	}


}
