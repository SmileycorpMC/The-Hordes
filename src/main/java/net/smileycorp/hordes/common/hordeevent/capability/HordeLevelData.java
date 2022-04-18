package net.smileycorp.hordes.common.hordeevent.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;

public class HordeLevelData extends SavedData {

	public static final String DATA = ModDefinitions.MODID + "_HordeEvent";

	private int nextDay = 0;

	protected Level level = null;

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

	public Map<Player, OngoingHordeEvent> getEvents() {
		Map<Player, OngoingHordeEvent> events = new HashMap<Player, OngoingHordeEvent>();
		if (!level.isClientSide) {
			for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent() &! player.isDeadOrDying()) {
					events.put(player, (OngoingHordeEvent) player.getCapability(Hordes.HORDE_EVENT, null).resolve().get());
				}
			}
		}
		return events;
	}

	public static HordeLevelData getData(ServerLevel level) {
		HordeLevelData data = (HordeLevelData) level.getChunkSource().getDataStorage().computeIfAbsent((nbt) -> fromNBT(level, nbt), () -> getCleanData(level), DATA);
		if (data.level == null) {
			data.level = level;
			int day = Math.round(level.getDayTime()/CommonConfigHandler.dayLength.get());
			if (!CommonConfigHandler.spawnFirstDay.get() && day <1) day  = 1;
			int multiplier = (int) Math.ceil(day / CommonConfigHandler.hordeSpawnDays.get());
			data.setNextDay((day * multiplier) + level.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1));
			data.setDirty();
		}
		return data;
	}

	protected static HordeLevelData getCleanData(Level level) {
		HordeLevelData data = new HordeLevelData();
		data.level = level;
		int day = Math.round(level.getDayTime()/CommonConfigHandler.dayLength.get());
		double multiplier = Math.ceil(day / CommonConfigHandler.hordeSpawnDays.get());
		if (!(CommonConfigHandler.spawnFirstDay.get() && day == 0)) multiplier += 1;
		int nextDay = (int) Math.floor(((multiplier*CommonConfigHandler.hordeSpawnDays.get()) + level.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1)));
		data.setNextDay(nextDay);
		return data;
	}

	protected static HordeLevelData fromNBT(Level level, CompoundTag nbt) {
		HordeLevelData data = getCleanData(level);
		if (nbt.contains("nextDay")) {
			int next = nbt.getInt("nextDay");
			if (data.nextDay == 0 || next > data.nextDay) {
				data.nextDay = next;
			}
		}
		return data;
	}

	public List<String> getDebugText() {
		List<String> out = new ArrayList<String>();
		out.add(this.toString());
		out.add("Existing events: {");
		for (Entry<Player,OngoingHordeEvent> entry : getEvents().entrySet()) {
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

}
