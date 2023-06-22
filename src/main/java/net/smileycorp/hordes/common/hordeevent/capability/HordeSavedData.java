package net.smileycorp.hordes.common.hordeevent.capability;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;

import java.util.*;
import java.util.Map.Entry;

public class HordeSavedData extends SavedData {

	public static final String DATA = "hordes";

	private int next_day = 0;

	protected Level level = null;

	private Map<UUID, HordeEvent> events = Maps.newHashMap();

	public void load(CompoundTag nbt) {
		if (nbt.contains("next_day")) {
			int next = nbt.getInt("next_day");
			if (next > next_day) {
				next_day = next;
			}
		}
		if (nbt.contains("events")) {
			CompoundTag events = nbt.getCompound("events");
			for (String uuid : events.getAllKeys()) {
				if (!DataUtils.isValidUUID(uuid)) return;
				HordeEvent horde = new HordeEvent();
				horde.readFromNBT(events.getCompound(uuid));
				this.events.put(UUID.fromString(uuid), horde);
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt.putInt("next_day", next_day);
		CompoundTag events = new CompoundTag();
		for (Entry<UUID, HordeEvent> entry : this.events.entrySet()) {
			events.put(entry.getKey().toString(), entry.getValue().writeToNBT(new CompoundTag()));
		}
		nbt.put("events", events);
		return nbt;
	}

	public int getNextDay() {
		return next_day;
	}

	public void setNextDay(int next_day) {
		this.next_day = next_day;
	}

	public void save() {
		setDirty();
		if (level instanceof ServerLevel) ((ServerLevel)level).getChunkSource().getDataStorage().set(DATA, this);
	}

	public HordeEvent getEvent(Player player) {
		return player == null ? null : getEvent(player.getUUID());
	}

	public HordeEvent getEvent(UUID uuid) {
		return uuid == null ? null : getEvent(uuid);
	}

	public List<String> getDebugText() {
		List<String> out = new ArrayList<>();
		out.add(toString());
		out.add("Existing events: {");
		for (Entry<UUID, HordeEvent> entry : events.entrySet()) {
			out.add("	" +entry.getValue().toString(getName(entry.getKey())));
			out.addAll(entry.getValue().getEntityStrings());
		}
		out.add("}");
		return out;
	}

	public String getName(UUID uuid) {
		Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
		if (player != null) return player.getName().getString();
		Optional<GameProfile> profile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
		if (profile.isPresent() && profile.get().getName() != null) return profile.get().getName();
		return uuid.toString();
	}

	@Override
	public String toString() {
		return super.toString() + "[current_day: " + (int)Math.floor((int)level.getDayTime()/(int)CommonConfigHandler.dayLength.get()) +
				", current_time: " + level.getDayTime()%(int)CommonConfigHandler.dayLength.get() + "next_day="+ next_day +"]";
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
		int day = Math.round(level.getDayTime()/CommonConfigHandler.dayLength.get());
		double multiplier = Math.ceil(day / CommonConfigHandler.hordeSpawnDays.get());
		if (!(CommonConfigHandler.spawnFirstDay.get() && day == 0)) multiplier += 1;
		int nextDay = (int) Math.floor(((multiplier*CommonConfigHandler.hordeSpawnDays.get()) + level.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1)));
		data.setNextDay(nextDay);
		return data;
	}


}
