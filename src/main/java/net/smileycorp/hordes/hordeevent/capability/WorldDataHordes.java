package net.smileycorp.hordes.hordeevent.capability;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;

import java.util.*;
import java.util.Map.Entry;

public class WorldDataHordes extends WorldSavedData {

	public static final String DATA = Constants.MODID + "_HordeEvent";
	
	private final Random rand = new Random();

	private int next_day = 0;

	protected World world = null;
	
	private Map<UUID, HordeEvent> events = Maps.newHashMap();

	public WorldDataHordes() {
		this(DATA);
	}

	public WorldDataHordes(String data) {
		super(data);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("nextDay")) {
			int next = nbt.getInteger("nextDay");
			if (next > next_day) next_day = next;
		}
		if (nbt.hasKey("events")) {
			NBTTagCompound events = nbt.getCompoundTag("events");
			for (String uuid : events.getKeySet()) {
				if (!DataUtils.isValidUUID(uuid)) return;
				HordeEvent horde = new HordeEvent(this);
				horde.readFromNBT(events.getCompoundTag(uuid));
				this.events.put(UUID.fromString(uuid), horde);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("nextDay", next_day);
		NBTTagCompound events = new NBTTagCompound();
		for (Entry<UUID, HordeEvent> entry : this.events.entrySet()) {
			UUID uuid = entry.getKey();
			NBTTagCompound tag = new NBTTagCompound();
			EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
			if (player != null) tag.setString("username", player.getName());
			events.setTag(uuid.toString(), entry.getValue().writeToNBT(tag));
		}
		nbt.setTag("events", events);
		return nbt;
	}

	public int getNextDay() {
		return next_day;
	}

	public void setNextDay(int next_day) {
		this.next_day = next_day;
	}
	
	public void save() {
		markDirty();
		if (world instanceof WorldServer) world.getMapStorage().setData(DATA, this);
	}
	
	public HordeEvent getEvent(EntityPlayerMP player) {
		return player == null ? null : getEvent(EntityPlayer.getUUID(player.getGameProfile()));
	}
	
	public HordeEvent getEvent(UUID uuid) {
		if (uuid == null) return null;
		if (!events.containsKey(uuid)) events.put(uuid, new HordeEvent(this));
		return events.get(uuid);
	}
	
	public String getName(UUID uuid) {
		EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
		if (player != null) return player.getName();
		GameProfile profile = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(uuid);
		if (profile != null && profile.getName() != null) return profile.getName();
		return uuid.toString();
	}
	
	public Random getRandom() {
		return rand;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[current_day: " + (int)Math.floor((float) world.getWorldTime() / (float) HordeEventConfig.dayLength) +
				", current_time: " + world.getWorldTime() % HordeEventConfig.dayLength + ", next_day="+ next_day +"]";
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
	
	public static WorldDataHordes getData(World world) {
		WorldDataHordes data = (WorldDataHordes) world.getMapStorage().getOrLoadData(WorldDataHordes.class, DATA);
		if (data == null) data = getCleanData(world);
		if (data.world == null) data.world = world;
		data.save();
		return data;
	}
	
	public static WorldDataHordes getCleanData(World world) {
		WorldDataHordes data = new WorldDataHordes();
		data.world = world;
		int day = Math.round(world.getWorldTime()/ HordeEventConfig.dayLength);
		double multiplier = Math.ceil(day / HordeEventConfig.hordeSpawnDays);
		if (!(HordeEventConfig.spawnFirstDay && day == 0)) multiplier += 1;
		int nextDay = (int) Math.floor(((multiplier* HordeEventConfig.hordeSpawnDays)
				+ world.rand.nextInt(HordeEventConfig.hordeSpawnVariation + 1)));
		data.setNextDay(nextDay);
		return data;
	}

}
