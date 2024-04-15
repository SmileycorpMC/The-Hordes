package net.smileycorp.hordes.hordeevent.capability;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;

import java.util.*;
import java.util.Map.Entry;

public class HordeSavedData extends WorldSavedData {
	
	public static final String DATA = Constants.MODID + "_HordeEvent";
	private final Random rand = new Random();
	private int next_day = 0;
	protected World level = null;
	
	private Map<UUID, HordeEvent> events = Maps.newHashMap();
	
	public HordeSavedData(String id) {
		super(id);
	}
	
	public HordeSavedData() {
		this(DATA);
	}
	
	@Override
	public void load(CompoundNBT nbt) {
		if (nbt.contains("next_day")) {
			int next = nbt.getInt("next_day");
			if (next > next_day) next_day = next;
		}
		if (nbt.contains("events")) {
			CompoundNBT events = nbt.getCompound("events");
			for (String uuid : events.getAllKeys()) {
				if (!DataUtils.isValidUUID(uuid)) return;
				HordeEvent horde = new HordeEvent(this);
				horde.readFromNBT(events.getCompound(uuid));
				this.events.put(UUID.fromString(uuid), horde);
			}
		}
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt.putInt("next_day", next_day);
		CompoundNBT events = new CompoundNBT();
		for (Entry<UUID, HordeEvent> entry : this.events.entrySet()) {
			UUID uuid = entry.getKey();
			CompoundNBT tag = new CompoundNBT();
			ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
			if (player != null) tag.putString("username", player.getName().getString());
			events.put(uuid.toString(), entry.getValue().writeToNBT(tag));
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
		if (level instanceof ServerWorld) ((ServerWorld)level).getChunkSource().getDataStorage().set(this);
	}
	
	public HordeEvent getEvent(ServerPlayerEntity player) {
		return player == null ? null : getEvent(player.getUUID());
	}
	
	public HordeEvent getEvent(UUID uuid) {
		if (uuid == null) return null;
		if (!events.containsKey(uuid)) events.put(uuid, new HordeEvent(this));
		return events.get(uuid);
	}
	
	public String getName(UUID uuid) {
		ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
		if (player != null) return player.getName().getString();
		GameProfile profile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
		if (profile != null && profile.getName() != null) return profile.getName();
		return uuid.toString();
	}
	
	public Random getRandom() {
		return rand;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[current_day: " + (int)Math.floor((float)level.getDayTime() / (float) HordeEventConfig.dayLength.get()) +
				", current_time: " + level.getDayTime() % HordeEventConfig.dayLength.get() + ", next_day="+ next_day +"]";
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
	
	public static HordeSavedData getData(ServerWorld level) {
		HordeSavedData data = level.getChunkSource().getDataStorage().computeIfAbsent(() -> getCleanData(level), DATA);
		if (data == null) data = getCleanData(level);
		level.getChunkSource().getDataStorage().set(data);
		return data;
	}
	
	public static HordeSavedData getCleanData(ServerWorld level) {
		HordeSavedData data = new HordeSavedData();
		data.level = level;
		int day = Math.round(level.getDayTime()/ HordeEventConfig.dayLength.get());
		double multiplier = Math.ceil(day / HordeEventConfig.hordeSpawnDays.get());
		if (!(HordeEventConfig.spawnFirstDay.get() && day == 0)) multiplier += 1;
		int nextDay = (int) Math.floor(((multiplier* HordeEventConfig.hordeSpawnDays.get())
				+ level.random.nextInt(HordeEventConfig.hordeSpawnVariation.get() + 1)));
		data.setNextDay(nextDay);
		return data;
	}
	
}
